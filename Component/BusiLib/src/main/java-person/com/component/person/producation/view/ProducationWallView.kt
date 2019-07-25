package com.component.person.producation.view

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout

import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.UserInfoModel
import com.common.player.IPlayer
import com.common.player.MyMediaPlayer
import com.common.player.VideoPlayerAdapter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.SpanUtils
import com.common.view.DebounceViewClickListener
import com.component.busilib.R
import com.component.person.producation.adapter.ProducationAdapter
import com.component.person.producation.model.ProducationModel
import com.component.person.view.RequestCallBack
import com.dialog.view.TipsDialogView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.component.dialog.ShareWorksDialog

import java.util.HashMap

import okhttp3.MediaType
import okhttp3.RequestBody

/**
 * 作品墙view
 */
class ProducationWallView(internal var mFragment: BaseFragment, var userInfoModel: UserInfoModel, internal var mCallBack: RequestCallBack?) : RelativeLayout(mFragment.context) {

    val TAG = "ProducationWallView"
    internal val mUserInfoServerApi: UserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    private val mProducationView: RecyclerView
    internal val mAdapter: ProducationAdapter

    private var mIPlayer: IPlayer? = null

    private var DEFAUAT_CNT = 20       // 默认拉取一页的数量
    internal var offset: Int = 0  // 拉照片偏移量
    private var mLastUpdateInfo: Long = 0    //上次更新成功时间

    internal var mConfirmDialog: DialogPlus? = null
    private var mShareWorksDialog: ShareWorksDialog? = null

    init {

        View.inflate(context, R.layout.producation_wall_view_layout, this)
        mProducationView = findViewById(R.id.producation_view)
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mProducationView.layoutManager = linearLayoutManager

        var isSelf = false
        if (userInfoModel.userId.toLong() == MyUserInfoManager.getInstance().uid) {
            isSelf = true
        }

        mAdapter = ProducationAdapter(isSelf)
        mAdapter.mOnClickDeleListener = { _, model ->
            if (model?.worksID == mAdapter.playingWorksIdPosition) {
                // 先停止播放
                stopPlay()
            }
            model?.let { showConfirmDialog(it) }
        }
        mAdapter.mOnClickShareListener = { _, model ->
            // TODO: 2019/5/22 弹出分享框 需不需要先停止音乐
            if (model?.worksID == mAdapter.playingWorksIdPosition) {
                // 先停止播放
                stopPlay()
            }
            model?.let { showShareDialog(it) }
        }

        mAdapter.mOnClickPlayListener = { view, play, position, model ->
            if (play) {
                if (model != null) {
                    if (mIPlayer == null) {
                        mIPlayer = MyMediaPlayer()
                        mIPlayer!!.setDecreaseVolumeEnd(true)
                        // 播放完毕
                        mIPlayer!!.setCallback(object : VideoPlayerAdapter.PlayerCallbackAdapter() {
                            override fun onCompletion() {
                                super.onCompletion()
                                mAdapter.setPlayPosition(-1)
                            }
                        })
                    }
                    mIPlayer?.reset()
                    mIPlayer?.startPlay(model.worksURL)
                    playProducation(model, position)
                    // 开始播放当前postion，
                    // 清楚上一个
                    mAdapter.setPlayPosition(model.worksID)
                }
            } else {
                if (mIPlayer != null) {
                    //mIPlayer.setCallback(null);
                    mIPlayer!!.pause()
                }
                // 不用刷新，优化下，防止闪动， icon 在 click 事件内部已经设置过了
                mAdapter.setPlayPosition(-1)
            }
        }
        mProducationView.adapter = mAdapter
    }

    private fun showConfirmDialog(model: ProducationModel) {
        val stringBuilder = SpanUtils()
                .append("确定删除该作品吗？")
                .create()
        val tipsDialogView = TipsDialogView.Builder(context)
                .setMessageTip(stringBuilder)
                .setConfirmTip("确认")
                .setCancelTip("我再想想")
                .setConfirmBtnClickListener(object : DebounceViewClickListener() {
                    override fun clickValid(v: View) {
                        if (mConfirmDialog != null) {
                            mConfirmDialog!!.dismiss()
                        }
                        deleteProducation(model)
                    }
                })
                .setCancelBtnClickListener(object : DebounceViewClickListener() {
                    override fun clickValid(v: View) {
                        if (mConfirmDialog != null) {
                            mConfirmDialog!!.dismiss()
                        }
                    }
                })
                .build()

        mConfirmDialog = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .create()
        mConfirmDialog!!.show()

    }

    private fun showShareDialog(model: ProducationModel) {
        mShareWorksDialog?.dismiss(false)
        mShareWorksDialog = ShareWorksDialog(mFragment, model.name, false)
        mShareWorksDialog?.setData(model.userID, model.nickName, model.cover, model.name, model.worksURL, model.worksID)
        mShareWorksDialog?.show()
    }

    fun stopPlay() {
        mAdapter.setPlayPosition(-1)
        if (mIPlayer != null) {
            mIPlayer!!.stop()
        }
    }

    fun getProducations(isFlag: Boolean) {
        val now = System.currentTimeMillis()
        if (!isFlag) {
            // 10分钟更新一次吧
            if (now - mLastUpdateInfo < 10 * 60 * 1000) {
                return
            }
        }

        getProducations(0)
    }

    fun getMoreProducations() {
        getProducations(offset)
    }

    private fun getProducations(offset: Int) {
        ApiMethods.subscribe(mUserInfoServerApi.getWorks(userInfoModel.userId, offset, DEFAUAT_CNT), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val totalCnt = result.data!!.getIntValue("totalCnt")
                    val newOffset = result.data!!.getIntValue("offset")
                    val list = JSON.parseArray(result.data!!.getString("works"), ProducationModel::class.java)
                    if (offset == 0) {
                        addProducation(list, newOffset, totalCnt, true)
                    } else {
                        addProducation(list, newOffset, totalCnt, false)
                    }
                } else {
                    loadProducationsFailed()
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
                loadProducationsFailed()
            }
        }, mFragment)
    }


    private fun deleteProducation(model: ProducationModel) {
        val map = HashMap<String, Any>()
        map["worksID"] = model.worksID
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mUserInfoServerApi.deleWorks(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    mAdapter.delete(model)
                    mAdapter.notifyDataSetChanged()
                }
            }
        }, mFragment)
    }

    fun playProducation(model: ProducationModel, position: Int) {
        val map = HashMap<String, Any>()
        map["toUserID"] = userInfoModel.userId
        map["worksID"] = model.worksID
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mUserInfoServerApi.playWorks(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    // TODO: 2019/5/22 播放次数客户端自己加一
                    model.playCnt = model.playCnt + 1
                    mAdapter.notifyDataSetChanged()
                }
            }
        }, mFragment, ApiMethods.RequestControl("playWorks", ApiMethods.ControlType.CancelThis))
    }

    private fun addProducation(list: List<ProducationModel>?, newOffset: Int, totalCnt: Int, isClear: Boolean) {
        offset = newOffset
        mLastUpdateInfo = System.currentTimeMillis()

        if (mCallBack != null) {
            mCallBack!!.onRequestSucess()
        }
        if (isClear) {
            mAdapter.dataList.clear()
        }

        if (list != null && list.isNotEmpty()) {
            mAdapter.dataList.addAll(list)
            mAdapter.notifyDataSetChanged()
        } else {
            if (mAdapter.dataList != null && mAdapter.dataList.size > 0) {
                // 没有更多了
            } else {
                // 没有数据
            }
        }
    }

    private fun loadProducationsFailed() {
        if (mCallBack != null) {
            mCallBack!!.onRequestSucess()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mIPlayer != null) {
            mIPlayer!!.setCallback(null)
            mIPlayer!!.stop()
            mIPlayer!!.release()
        }
        if (mShareWorksDialog != null) {
            mShareWorksDialog!!.dismiss(false)
        }
        if (mConfirmDialog != null) {
            mConfirmDialog!!.dismiss(false)
        }
    }
}
