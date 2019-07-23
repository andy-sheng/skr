package com.module.feeds.watch.view

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.log.MyLog
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.watch.adapter.FeedsWatchViewAdapter
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.presenter.FeedWatchViewPresenter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

class FeedsWatchView(var fragment: BaseFragment, var type: Int) : ConstraintLayout(fragment.context), IFeedsWatchView {
    companion object {
        const val TYPE_RECOMMEND = 1
        const val TYPE_FOLLOW = 2
    }

    private val mRefreshLayout: SmartRefreshLayout
    private val mClassicsHeader: ClassicsHeader
    private val mRecyclerView: RecyclerView

    private val mAdapter: FeedsWatchViewAdapter
    private val mPersenter: FeedWatchViewPresenter

    init {
        View.inflate(context, R.layout.feed_watch_view_layout, this)

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mClassicsHeader = findViewById(R.id.classics_header)
        mRecyclerView = findViewById(R.id.recycler_view)

        mAdapter = FeedsWatchViewAdapter()
        mPersenter = FeedWatchViewPresenter(this, type)

        mRefreshLayout.setEnableRefresh(true)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        mRefreshLayout.setEnableOverScrollDrag(true)
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                mPersenter.initWatchList(true)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                mPersenter.loadMoreWatchList()
            }
        })

        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        mAdapter.onClickMoreListener = {
            // 更多
        }

        mAdapter.onClickLikeListener = {
            // 喜欢
        }

        mAdapter.onClickCommentListener = {
            // 评论
            ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                    .navigation()
        }

        mAdapter.onClickHitListener = {
            // 打榜
        }

        mAdapter.onClickDetailListener = {
            // 详情
            ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                    .navigation()
        }

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
        mAdapter.notifyDataSetChanged()
    }

    override fun requestError() {

    }

    fun destory() {
        mPersenter.destroy()
    }
}