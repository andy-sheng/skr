package com.module.feeds.watch.view

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING
import android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING
import android.view.View
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.watch.adapter.FeedsWatchViewAdapter
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.presenter.FeedWatchViewPresenter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlin.math.max

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

    init {
        View.inflate(context, R.layout.feed_watch_view_layout, this)

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mClassicsHeader = findViewById(R.id.classics_header)
        mRecyclerView = findViewById(R.id.recycler_view)

        mAdapter = FeedsWatchViewAdapter()
        mPersenter = FeedWatchViewPresenter(this, type)

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
                    SCROLL_STATE_IDLE -> {
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

                        model?.let {
                            isFound = true
                            play(it)
                        }
                    }
                    SCROLL_STATE_DRAGGING -> {
                    }
                    SCROLL_STATE_SETTLING -> {
                    }
                }
            }
        })

        mAdapter.onClickMoreListener = {
            // 更多
        }

        mAdapter.onClickLikeListener = {
            // 喜欢
        }

        mAdapter.onClickCommentListener = {
            // 评论
            ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                    .withSerializable("feed_model", it)
                    .navigation()
        }

        mAdapter.onClickHitListener = {
            // 打榜
            it?.song?.let {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_MAKE)
                        .withSerializable("song_model", it)
                        .navigation()
            }

        }

        mAdapter.onClickDetailListener = {
            // 详情
            ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                    .withSerializable("feed_model", it)
                    .navigation()
        }

    }

    fun stopPlay() {
        mAdapter.mCurrentModel = null
        mAdapter.notifyDataSetChanged()
    }

    private fun play(model: FeedsWatchModel) {
        if (mAdapter.mCurrentModel != model) {
            mAdapter.mCurrentModel = model
            mAdapter.notifyDataSetChanged()
        }
        mCurrentModel = model
    }

    fun initData(flag: Boolean) {
        mPersenter.initWatchList(flag)
    }

    override fun addWatchList(list: List<FeedsWatchModel>, isClear: Boolean) {
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()
        if (isClear) {
            mAdapter.mDataList.clear()
        }
        mAdapter.mDataList.addAll(list)
        if (isClear) {
            play(mAdapter.mDataList[0])
        }
        mAdapter.notifyDataSetChanged()
    }

    // 请求时间太短，不向服务器请求，只需要恢复上次播放
    override fun requestTimeShort() {
        mCurrentModel?.let {
            play(it)
        }
    }

    override fun requestError() {
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()
    }

    fun destory() {
        mPersenter.destroy()
    }
}