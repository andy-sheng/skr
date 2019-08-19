package com.module.feeds.watch.watchview

import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.component.busilib.callback.EmptyCallback
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
        if (it.user?.userID == MyUserInfoManager.getInstance().uid.toInt()) {
            mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_FEED_HOME, it, true)
            mFeedsMoreDialogView?.showByDialog()
        } else {
            mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_FEED_HOME, it, null)
            mFeedsMoreDialogView?.showByDialog()
        }
    }

    override fun resumePlay() {
        super.resumePlay()
        srollPositionToTop(mAdapter.mCurrentPlayPosition ?: 0)
    }

    override fun recyclerIdlePosition(position: Int) {
        controlPlay(position, mAdapter.mDataList[position], true)
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

    override fun getMoreFeeds() {
        getFollowFeedList(mOffset, true)
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

    private fun getFollowFeedList(offset: Int, isClear: Boolean) {
        launch {
            val result = subscribe(RequestControl("getFollowFeedList", ControlType.CancelThis)) {
                mFeedServerApi.getFeedFollowList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt())
            }
            if (result.errno == 0) {
                mHasInitData = true
                val list = JSON.parseArray(result.data.getString("follows"), FeedsWatchModel::class.java)
                mOffset = result.data.getIntValue("offset")
                hasMore = result.data.getBoolean("hasMore")
                finishRefreshOrLoadMore()
                addFollowWatchList(list, isClear)
            } else {
                finishRefreshOrLoadMore()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun addFollowWatchList(list: List<FeedsWatchModel>?, isClear: Boolean) {
        if (isClear) {
            mAdapter.mDataList.clear()
            if (list != null && list.isNotEmpty()) {
                mAdapter.mDataList.addAll(list)
            }
            mAdapter.notifyDataSetChanged()
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
            if (list != null && list.isNotEmpty()) {
                mAdapter.mDataList.addAll(list)
                mAdapter.notifyDataSetChanged()
            }
        }

        if (mAdapter.mDataList != null && mAdapter.mDataList.isNotEmpty()) {
            mLoadService?.showSuccess()
        } else {
            mLoadService?.showCallback(EmptyCallback::class.java)
        }

    }
}