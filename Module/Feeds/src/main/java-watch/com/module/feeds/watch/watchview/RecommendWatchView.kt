package com.module.feeds.watch.watchview

import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.videocache.MediaCacheManager
import com.component.busilib.callback.EmptyCallback
import com.module.feeds.watch.model.FeedRecommendTagModel
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsMoreDialogView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecommendWatchView(fragment: BaseFragment) : BaseWatchView(fragment, TYPE_RECOMMEND) {

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
        var realPosition = position  // 在列表中的实际位置
        if (position == 0) {
            realPosition = 1
        }
        val model = if (!mAdapter.mDataList.isNullOrEmpty() && realPosition - 1 >= 0 && realPosition - 1 < mAdapter.mDataList.size) {
            mAdapter.mDataList[realPosition - 1]
        } else {
            null
        }
        model?.let {
            controlPlay(realPosition, model, true)
        }
    }

    override fun onPreparedMusic() {
        if (mAdapter.mCurrentPlayPosition in 0 until mAdapter.mDataList.size) {
            // mCurrentPlayPosition 比在list中大1
            mAdapter.mDataList[mAdapter.mCurrentPlayPosition].song?.playURL?.let { it2 ->
                MediaCacheManager.preCache(it2)
            }
        }
    }

    override fun findPreSong(userAction: Boolean): FeedSongModel? {
        // 推荐
        if (mAdapter.mCurrentPlayPosition < 2) {
            return null
        }

        if (!mAdapter.mDataList.isNullOrEmpty()) {
            if (mAdapter.mCurrentPlayPosition in 2..(mAdapter.mDataList.size + 1)) {
                mAdapter.mCurrentPlayPosition = mAdapter.mCurrentPlayPosition - 1
                mAdapter.mCurrentPlayModel = mAdapter.mDataList[mAdapter.mCurrentPlayPosition - 1]
                return if (mAdapter.mCurrentPlayModel?.status != 2) {
                    // 未审核通过
                    findPreSong(userAction)
                } else {
                    mAdapter.mCurrentPlayModel?.song
                }
            }
        }

        return null
    }

    override fun findNextSong(userAction: Boolean): FeedSongModel? {
        // 补充推荐
        if (mAdapter.mCurrentPlayPosition == mAdapter.mDataList.size - 1) {
            getMoreFeeds()
        }

        if (!mAdapter.mDataList.isNullOrEmpty()) {
            // 在合理范围内
            if (mAdapter.mCurrentPlayPosition in 0 until mAdapter.mDataList.size) {
                mAdapter.mCurrentPlayPosition = mAdapter.mCurrentPlayPosition + 1
                mAdapter.mCurrentPlayModel = mAdapter.mDataList[mAdapter.mCurrentPlayPosition - 1]
                return if (mAdapter.mCurrentPlayModel?.status != 2) {
                    // 继续找下一个
                    findNextSong(userAction)
                } else {
                    mAdapter.mCurrentPlayModel?.song
                }
            }
        }

        return null
    }

    override fun getMoreFeeds() {
        getRecommendFeedList(mOffset, false)
    }

    override fun initFeedList(flag: Boolean): Boolean {
        if (!flag && mHasInitData) {
            // 不一定要刷新
            return false
        }
        // todo 记得优化一下
        getRecommendFeedList(0, true)
        getRecommendTagList()
        return true
    }

    private fun getRecommendTagList() {
        launch {
            val obj = subscribe(RequestControl("getRecomendTagList", ControlType.CancelThis)) {
                mFeedServerApi.getRecomendTagList(0, 4, MyUserInfoManager.getInstance().uid)
            }
            if (obj.errno == 0) {
                val list = JSON.parseArray(obj.data.getString("tags"), FeedRecommendTagModel::class.java)
                mAdapter.mRankTagList.clear()
                if (!list.isNullOrEmpty()) {
                    mAdapter.mRankTagList.addAll(list)
                }
                mAdapter.notifyDataSetChanged()
            } else {
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun getRecommendFeedList(offset: Int, isClear: Boolean) {
        launch {
            val obj = subscribe(RequestControl("getRecommendFeedList", ControlType.CancelThis)) {
                mFeedServerApi.getFeedRecommendList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt())
            }
            if (obj.errno == 0) {
                mHasInitData = true
                val list = JSON.parseArray(obj.data.getString("recommends"), FeedsWatchModel::class.java)
                mOffset = obj.data.getIntValue("offset")
                hasMore = obj.data.getBoolean("hasMore")
                finishRefreshOrLoadMore()
                addRecommendWatchList(list, isClear)
            } else {
                finishRefreshOrLoadMore()
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun addRecommendWatchList(list: List<FeedsWatchModel>?, isClear: Boolean) {
        if (isClear) {
            mAdapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                mAdapter.mDataList.addAll(list)
            }
            mAdapter.notifyDataSetChanged()
            srollPositionToTop(0)

            if (mAdapter.mDataList.isNotEmpty()) {
                // delay 是因为2哥notify冲突
                launch {
                    delay(200)
                    controlPlay(1, mAdapter.mDataList.get(0), true)
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


    override fun destroy() {
        super.destroy()
        mFeedsMoreDialogView?.dismiss(false)
    }
}