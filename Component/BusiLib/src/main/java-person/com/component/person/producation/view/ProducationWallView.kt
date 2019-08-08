package com.component.person.producation.view

import android.media.MediaPlayer
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.UserInfoModel
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.rxretrofit.*
import com.common.utils.SpanUtils
import com.common.view.DebounceViewClickListener
import com.component.busilib.R
import com.component.dialog.ShareWorksDialog
import com.component.person.producation.adapter.ProducationAdapter
import com.component.person.producation.model.ProducationModel
import com.component.person.view.RequestCallBack
import com.dialog.view.TipsDialogView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*

/**
 * 作品墙view
 */
class ProducationWallView(internal var mFragment: BaseFragment, var userInfoModel: UserInfoModel, internal var mCallBack: RequestCallBack?) : RelativeLayout(mFragment.context) {

    val TAG = "ProducationWallView"
    val playerTag = TAG + hashCode()
    internal val mUserInfoServerApi: UserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    private val mProducationView: RecyclerView
    internal val mAdapter: ProducationAdapter

    private var DEFAUAT_CNT = 20       // 默认拉取一页的数量
    internal var offset: Int = 0  // 拉照片偏移量
    private var mLastUpdateInfo: Long = 0    //上次更新成功时间
    var hasMore = true

    internal var mConfirmDialog: DialogPlus? = null
    private var mShareWorksDialog: ShareWorksDialog? = null
    val playCallback: PlayerCallbackAdapter

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

        playCallback = object : PlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                mAdapter.setPlayPosition(-1)
            }

            override fun openTimeFlyMonitor(): Boolean {
                return true
            }

            override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {

            }

            override fun onTimeFlyMonitor(pos: Long, duration: Long) {

            }
        }
        SinglePlayer.addCallback(playerTag, playCallback)

        mAdapter.mOnClickPlayListener = { view, play, position, model ->
            if (play) {
                if (model != null) {
                    SinglePlayer.startPlay(playerTag, model?.worksURL ?: "")
                    playProducation(model, position)
                    // 开始播放当前postion，
                    // 清楚上一个
                    mAdapter.setPlayPosition(model.worksID)
                }
            } else {
                SinglePlayer.pause(playerTag)
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
        SinglePlayer.stop(playerTag)
    }

    fun getProducations(isFlag: Boolean) {
        val now = System.currentTimeMillis()
        if (!isFlag && mAdapter.dataList.isNotEmpty()) {
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
        }, mFragment, RequestControl("playWorks", ControlType.CancelThis))
    }

    private fun addProducation(list: List<ProducationModel>?, newOffset: Int, totalCnt: Int, isClear: Boolean) {
        offset = newOffset
        mLastUpdateInfo = System.currentTimeMillis()

        hasMore = !list.isNullOrEmpty()
        mCallBack?.onRequestSucess(hasMore)

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
        mCallBack?.onRequestSucess(true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SinglePlayer.stop(playerTag)
        if (mShareWorksDialog != null) {
            mShareWorksDialog!!.dismiss(false)
        }
        if (mConfirmDialog != null) {
            mConfirmDialog!!.dismiss(false)
        }
    }

    fun destory() {
        SinglePlayer.reset(playerTag)
        SinglePlayer.removeCallback(playerTag)
    }
}
