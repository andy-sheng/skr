package com.module.feeds.watch.watchview

import android.view.View
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.component.busilib.callback.EmptyCallback
import com.component.busilib.event.FeedPublishSucessEvent
import com.component.person.view.RequestCallBack
import com.dialog.view.TipsDialogView
import com.module.feeds.IPersonFeedsWall
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsMoreDialogView
import com.module.feeds.watch.view.FeedsWatchView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.HashMap

class PersonWatchView(fragment: BaseFragment, var userInfoModel: UserInfoModel, val callBack: RequestCallBack?) : BaseWatchView(fragment, TYPE_PERSON), IPersonFeedsWall {
    var mFeedsMoreDialogView: FeedsMoreDialogView? = null
    var mTipsDialogView: TipsDialogView? = null

    private var mOffset = 0   //偏移量
    private val mCNT = 20  // 默认拉去的个数

    override fun selected() {
        super.selected()
        initFeedList(false)
    }

    override fun clickMore(position: Int, it: FeedsWatchModel) {
        if (userInfoModel.userId.toLong() == MyUserInfoManager.getInstance().uid) {
            mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_PERSON, it, null)
                    .apply {
                        mCopyReportTv.text = "删除"
                        mCopyReportTv.setOnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                dismiss(false)
                                mTipsDialogView = TipsDialogView.Builder(fragment.activity)
                                        .setMessageTip("是否确定删除该神曲")
                                        .setConfirmTip("确认删除")
                                        .setCancelTip("取消")
                                        .setCancelBtnClickListener(object : AnimateClickListener() {
                                            override fun click(view: View?) {
                                                mTipsDialogView?.dismiss()
                                            }
                                        })
                                        .setConfirmBtnClickListener(object : AnimateClickListener() {
                                            override fun click(view: View?) {
                                                mTipsDialogView?.dismiss(false)
                                                deleteFeed(position, it)
                                            }
                                        })
                                        .build()
                                mTipsDialogView?.showByDialog()
                            }
                        })
                    }
        } else {
            mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_OTHER_PERSON, it, null)
        }
        mFeedsMoreDialogView?.showByDialog()
    }

    override fun findPresong(userAction: Boolean): FeedSongModel? {
        if (mAdapter.mCurrentPlayPosition == 0) {
            return null
        }

        mAdapter.mCurrentPlayPosition?.let {
            if (!mAdapter.mDataList.isNullOrEmpty()) {
                mAdapter.mCurrentPlayPosition = it - 1
                mAdapter.mCurrentPlayModel = mAdapter.mDataList[mAdapter.mCurrentPlayPosition
                        ?: 0]
                return if (mAdapter.mCurrentPlayModel?.status != 2) {
                    // 未审核通过
                    findPresong(userAction)
                } else {
                    mAdapter.mCurrentPlayModel?.song
                }
            }
        }
        return null
    }

    override fun findNextSong(userAction: Boolean): FeedSongModel? {
        if (mAdapter.mCurrentPlayPosition == mAdapter.mDataList.size - 2) {
            // 已经到最后一个，需要去更新数据
            getMoreFeeds()
        }

        if (mAdapter.mCurrentPlayPosition != null && mAdapter.mCurrentPlayPosition!! < mAdapter.mDataList.size - 1) {
            // 在合理范围内
            mAdapter.mCurrentPlayPosition = mAdapter.mCurrentPlayPosition!! + 1
            mAdapter.mCurrentPlayModel = mAdapter.mDataList[mAdapter.mCurrentPlayPosition!!]
            return if (mAdapter.mCurrentPlayModel?.status != 2) {
                // 继续找下一个
                findNextSong(userAction)
            } else {
                mAdapter.mCurrentPlayModel?.song
            }
        }

        return null
    }

    override fun getFeeds(flag: Boolean) {
        initFeedList(flag)
    }

    override fun initFeedList(flag: Boolean): Boolean {
        if (!flag && mHasInitData) {
            // 不一定要刷新
            return false
        }
        getPersonFeedList(0, true)
        return true
    }

    override fun getMoreFeeds() {
        getPersonFeedList(mOffset, false)
    }

    override fun recyclerIdlePosition(position: Int) {
        //TODO 个人中心不需要自动播放
    }

    override fun destroy() {
        super.destroy()
        mFeedsMoreDialogView?.dismiss(false)
    }

    override fun setUserInfoModel(any: Any?) {
        val model = any as UserInfoModel?
        model?.let { userInfoModel = model }
    }

    override fun isHasMore(): Boolean {
        return hasMore
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedPublishSucessEvent) {
        // 新的发布,重新去刷新下数据
        mHasInitData = false
    }

    private fun getPersonFeedList(offset: Int, isClear: Boolean) {
        var feedSongType = 1
        if (MyUserInfoManager.getInstance().uid.toInt() != userInfoModel.userId) {
            feedSongType = 2
        }
        launch {
            var result = subscribe(RequestControl("getPersonFeedList", ControlType.CancelThis)) {
                mFeedServerApi.queryFeedsList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt(), userInfoModel.userId
                        ?: 0, feedSongType)
            }
            if (result.errno == 0) {
                mOffset = result.data.getIntValue("offset")
                hasMore = result.data.getBoolean("hasMore")
                val list = JSON.parseArray(result.data.getString("userSongs"), FeedsWatchModel::class.java)
                finishRefreshOrLoadMore()
                addPersonWatchList(list, isClear)
            } else {
                finishRefreshOrLoadMore()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun addPersonWatchList(list: List<FeedsWatchModel>?, isClear: Boolean) {
        callBack?.onRequestSucess(hasMore)
        if (isClear) {
            mAdapter.mDataList.clear()
            if (list != null && list.isNotEmpty()) {
                mAdapter.mDataList.addAll(list)
            }
            mAdapter.notifyDataSetChanged()
            srollPositionToTop(0)
        } else {
            if (list != null && list.isNotEmpty()) {
                mAdapter.mDataList.addAll(list)
                mAdapter.notifyDataSetChanged()
            }
        }
    }
}