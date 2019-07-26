package com.module.feeds.rank.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.module.feeds.R
import com.module.feeds.rank.adapter.FeedsRankAdapter
import com.module.feeds.rank.model.FeedRankTagModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

class FeedsRankView(context: Context, tag: FeedRankTagModel) : ConstraintLayout(context) {

    private val mRefreshLayout: SmartRefreshLayout
    private val mRecyclerView: RecyclerView
    private val mAdapter = FeedsRankAdapter()

    var offset = 0
    var cnt = 30

    init {
        View.inflate(context, R.layout.feed_rank_view_layout, this)

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mRecyclerView = findViewById(R.id.recycler_view)

        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true)
        mRefreshLayout.setEnableOverScrollDrag(true)

        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                loadMoreData()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
            }
        })

        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    fun tryLoadData() {

    }

    fun loadMoreData() {

    }


}