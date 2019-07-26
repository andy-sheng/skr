package com.module.feeds.watch.view

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.AbsListView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.userinfo.UserInfoManager
import com.common.player.IPlayer
import com.common.player.MyMediaPlayer
import com.common.player.VideoPlayerAdapter
import com.common.view.DebounceViewClickListener
import com.component.dialog.FeedsMoreDialogView
import com.module.feeds.watch.presenter.FeedWatchViewPresenter
import com.module.feeds.watch.adapter.FeedsWatchViewAdapter
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.RouterConstants
import com.module.feeds.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener


class FeedsWatchView(fragment: BaseFragment, type: Int) : ConstraintLayout(fragment.context), IFeedsWatchView {
    companion object {
        const val TYPE_RECOMMEND = 1
        const val TYPE_FOLLOW = 2
    }

    private val mRefreshLayout: SmartRefreshLayout
    private val mClassicsHeader: ClassicsHeader
    private val mLayoutManager: LinearLayoutManager
    private val mRecyclerView: RecyclerView

    private val mAdapter: FeedsWatchViewAdapter
    private val mPersenter: FeedWatchViewPresenter

    private var mCurrentModel: FeedsWatchModel? = null  // 保存

    private var mMediaPlayer: IPlayer? = null

    var mFeedsMoreDialogView: FeedsMoreDialogView? = null

    init {
        View.inflate(context, R.layout.feed_watch_view_layout, this)

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mClassicsHeader = findViewById(R.id.classics_header)
        mRecyclerView = findViewById(R.id.recycler_view)

        mPersenter = FeedWatchViewPresenter(this, type)
        mAdapter = FeedsWatchViewAdapter(object : FeedsListener {
            override fun onclickRankListener(watchModel: FeedsWatchModel?) {
                // 排行榜详情
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_RANK_DETAIL)
                        .navigation()
            }

            override fun onClickLikeListener(position: Int, watchModel: FeedsWatchModel?) {
                // 喜欢
                watchModel?.let { mPersenter.feedLike(position, it) }
            }

            override fun onClickCommentListener(watchModel: FeedsWatchModel?) {
                // 评论
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                        .withSerializable("feed_model", watchModel)
                        .navigation()
            }

            override fun onClickHitListener(watchModel: FeedsWatchModel?) {
                // 打榜
                watchModel?.song?.let {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_MAKE)
                            .withSerializable("song_model", it)
                            .navigation()
                }
            }

            override fun onClickDetailListener(watchModel: FeedsWatchModel?) {
                // 详情
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                        .withSerializable("feed_model", watchModel)
                        .navigation()
            }

            override fun onClickCDListener(watchModel: FeedsWatchModel?) {
                // 播放
                watchModel?.let { model -> play(model, false) }
            }

            override fun onClickMoreListener(watchModel: FeedsWatchModel?) {
                // 更多
                watchModel?.let {
                    if (fragment.activity != null) {
                        mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_FEED)
                                .apply {
                                    setFollow(it.hasFollow == true)
                                    mFuncationTv.setOnClickListener(object : DebounceViewClickListener() {
                                        override fun clickValid(v: View?) {
                                            dismiss()
                                            UserInfoManager.getInstance().mateRelation(it.user?.userID
                                                    ?: 0, UserInfoManager.RA_BUILD, false)
                                        }
                                    })
                                }
                        mFeedsMoreDialogView?.showByDialog()
                    }

                }
            }

        })

        mRefreshLayout.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)
            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    mPersenter.initWatchList(true)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    mPersenter.loadMoreWatchList()
                }
            })
        }

        mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        (mRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            var maxPercent = 0f
            var model: FeedsWatchModel? = null
            var isFound = false

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {
                        var firstCompleteItem = mLayoutManager.findFirstCompletelyVisibleItemPosition()
                        if (firstCompleteItem != RecyclerView.NO_POSITION) {
                            // 找到了
                            model = mAdapter.mDataList[firstCompleteItem]
                        } else {
                            // 找不到位置，取其中百分比最大的
                            var firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()
                            var lastVisibleItem = mLayoutManager.findLastVisibleItemPosition()
                            val percents = FloatArray(lastVisibleItem - firstVisibleItem + 1)
                            var i = firstVisibleItem
                            isFound = false
                            maxPercent = 0f
                            model = null
                            while (i <= lastVisibleItem && !isFound) {
                                val itemView = mRecyclerView.findViewHolderForAdapterPosition(i).itemView
                                val location1 = IntArray(2)
                                val location2 = IntArray(2)
                                itemView.getLocationOnScreen(location1)
                                mRecyclerView.getLocationOnScreen(location2)
                                val top = location1[1] - location2[1]
                                when {
                                    top < 0 -> percents[i - firstVisibleItem] = (itemView.height + top).toFloat() * 100 / itemView.height
                                    (top + itemView.height) < mRecyclerView.height -> percents[i - firstVisibleItem] = 100f
                                    else -> percents[i - firstVisibleItem] = (mRecyclerView.height - top).toFloat() * 100 / itemView.height
                                }
                                if (percents[i - firstVisibleItem] == 100f) {
                                    isFound = true
                                    maxPercent = 100f
                                    model = mAdapter.mDataList[i]
                                } else {
                                    if (percents[i - firstVisibleItem] > maxPercent) {
                                        maxPercent = percents[i - firstVisibleItem]
                                        model = mAdapter.mDataList[i]
                                    }
                                }
                                i++
                            }
                        }

                        model?.let {
                            isFound = true
                            play(it, true)
                        }
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {

                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {

                    }
                }
            }
        })


    }

    private fun play(model: FeedsWatchModel, isMustPlay: Boolean) {
        if (isMustPlay) {
            // 播放
            play(model)
        } else {
            if (mAdapter.mCurrentModel?.feedID == model.feedID) {
                // 停止播放
                stop()
            } else {
                // 播放
                play(model)
            }
        }
    }

    private fun play(model: FeedsWatchModel) {
        if (mAdapter.mCurrentModel != model) {
            mAdapter.mCurrentModel = model
            mAdapter.notifyDataSetChanged()
        }
        mCurrentModel = model

        if (mMediaPlayer == null) {
            mMediaPlayer = MyMediaPlayer()
        }
        mMediaPlayer?.setCallback(object : VideoPlayerAdapter.PlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                // 重复播放
                model?.song?.playURL?.let {
                    mMediaPlayer?.startPlay(it)
                }
            }
        })
        model.song?.playURL?.let {
            mMediaPlayer?.startPlay(it)
        }
    }

    private fun stop() {
        mAdapter.mCurrentModel = null
        mAdapter.notifyDataSetChanged()
        mCurrentModel = null
        mMediaPlayer?.reset()
    }

    fun initData(flag: Boolean) {
        mPersenter.initWatchList(flag)
    }

    fun stopPlay() {
        // 保留mCurrentModel 可以用来恢复页面播放
        mAdapter.mCurrentModel = null
        mAdapter.notifyDataSetChanged()
        mMediaPlayer?.reset()
    }

    override fun addWatchList(list: List<FeedsWatchModel>?, isClear: Boolean) {
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()
        if (isClear) {
            mAdapter.mDataList.clear()
            if (list != null && list.isNotEmpty()) {
                mAdapter.mDataList.addAll(list)
            }
            if (mAdapter.mDataList.isNotEmpty()) {
                play(mAdapter.mDataList[0], true)
            }
            mAdapter.notifyDataSetChanged()
        } else {
            if (list != null && list.isNotEmpty()) {
                mAdapter.mDataList.addAll(list)
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    // 请求时间太短，不向服务器请求，只需要恢复上次播放
    override fun requestTimeShort() {
        mCurrentModel?.let {
            play(it, true)
        }
    }

    override fun requestError() {
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()
    }

    override fun feedLikeResult(position: Int, model: FeedsWatchModel, isLike: Boolean) {
        model.isLiked = isLike
        if (isLike) {
            model.starCnt = model.starCnt?.plus(1)
        } else {
            model.starCnt = model.starCnt?.minus(1)
        }
        mAdapter.update(position, model, FeedsWatchViewAdapter.REFRESH_TYPE_LIKE)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destory()
    }

    fun destory() {
        mPersenter.destroy()
        mMediaPlayer?.release()
    }
}