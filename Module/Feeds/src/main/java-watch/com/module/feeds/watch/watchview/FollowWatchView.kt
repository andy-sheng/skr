package com.module.feeds.watch.watchview

import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.log.MyLog
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.videocache.MediaCacheManager
import com.component.busilib.callback.EmptyCallback
import com.module.feeds.detail.manager.add2SongPlayModeManager
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsMoreDialogView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FollowWatchView(fragment: BaseFragment) : BaseWatchView(fragment, TYPE_FOLLOW) {

    var mFeedsMoreDialogView: FeedsMoreDialogView? = null

    private var mOffset = 0   //偏移量
    private val mCNT = 20  // 默认拉去的个数

    override fun selected() {
        super.selected()
        // 该页面选中以及从详情页返回都会回调这个方法
        if (!initFeedList(false)) {
            // 如果因为时间短没请求，继续往前播放,只有在首页才播
            resumePlay()
        }
    }

    override fun clickMore(position: Int, it: FeedsWatchModel) {
        mFeedsMoreDialogView?.dismiss(false)
        if (it.user?.userId == MyUserInfoManager.getInstance().uid.toInt()) {
            mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_FEED_HOME, it, true)
            mFeedsMoreDialogView?.showByDialog()
        } else {
            mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_FEED_HOME, it, null)
            mFeedsMoreDialogView?.showByDialog()
        }
    }

    override fun resumePlay() {
        super.resumePlay()
        srollPositionToTop(mAdapter.mCurrentPlayPosition)
    }

    override fun recyclerIdlePosition(position: Int) {
        controlPlay(position, mAdapter.mDataList[position], true)
    }

    override fun onPreparedMusic() {
//        if (mAdapter.mCurrentPlayPosition in -1..(mAdapter.mDataList.size - 2)) {
//            mAdapter.mDataList[mAdapter.mCurrentPlayPosition + 1].song?.playURL?.let { it2 ->
//                MediaCacheManager.preCache(it2)
//            }
//        }
    }

    override fun getMoreFeeds(dataOkCallback: (() -> Unit)?) {
        if (hasMore) {
            getFollowFeedList(mOffset, false, dataOkCallback)
        } else {
            MyLog.d(TAG, "getMoreFeeds hasMore = false")
        }
    }

    override fun initFeedList(flag: Boolean): Boolean {
        if (!flag && mHasInitData) {
            // 不一定要刷新
            return false
        }
        getFollowFeedList(0, true)
        return true
    }

    override fun destroy() {
        super.destroy()
        mFeedsMoreDialogView?.dismiss(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        // 关注的人有变化了
        mHasInitData = false
    }

    private fun getFollowFeedList(offset: Int, isClear: Boolean, dataOkCallback: (() -> Unit)? = null) {
        launch {
            val result = subscribe(RequestControl("getFollowFeedList", ControlType.CancelThis)) {
                mFeedServerApi.getFeedFollowList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt())
            }
            if (result.errno == 0) {
                mHasInitData = true
                val list = JSON.parseArray(result.data.getString("follows"), FeedsWatchModel::class.java)
                mOffset = result.data.getIntValue("offset")
                hasMore = result.data.getBoolean("hasMore")
                mSongPlayModeManager?.supportCycle = !hasMore
                finishRefreshOrLoadMore()
                addFollowWatchList(list, isClear)
            } else {
                finishRefreshOrLoadMore()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
            dataOkCallback?.invoke()
        }
    }

    private fun addFollowWatchList(list: List<FeedsWatchModel>?, isClear: Boolean) {
        if (isClear) {
            mAdapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                mAdapter.mDataList.addAll(list)
            }
            mAdapter.notifyDataSetChanged()
            add2SongPlayModeManager(mSongPlayModeManager, mAdapter.mDataList, isClear)
            srollPositionToTop(0)
            if (mAdapter.mDataList.isNotEmpty()) {
                // delay 是因为2哥notify冲突
                launch {
                    delay(200)
                    controlPlay(0, mAdapter.mDataList.get(0), true)
                }
            } else {
                // 拉回来的列表为空
                pausePlay()
                mAdapter.mCurrentPlayModel = null
            }
        } else {
            if (!list.isNullOrEmpty()) {
                mAdapter.mDataList.addAll(list)
                mAdapter.notifyDataSetChanged()
                add2SongPlayModeManager(mSongPlayModeManager, list, isClear)
            }
        }

        if (mAdapter.mDataList != null && mAdapter.mDataList.isNotEmpty()) {
            mLoadService?.showSuccess()
        } else {
            mLoadService?.showCallback(EmptyCallback::class.java)
        }

    }
}