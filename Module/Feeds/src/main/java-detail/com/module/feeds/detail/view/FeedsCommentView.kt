package com.module.feeds.detail.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.common.view.ex.ExConstraintLayout
import com.module.feeds.detail.adapter.FeedsCommentAdapter
import com.module.feeds.detail.inter.IFirstLevelCommentView
import com.module.feeds.detail.model.FirstLevelCommentModel
import com.module.feeds.detail.presenter.FeedsCommentPresenter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

class FeedsCommentView : ExConstraintLayout, IFirstLevelCommentView {
    val mRefreshLayout: SmartRefreshLayout
    val mRecyclerView: RecyclerView
    lateinit var mFeedsCommentPresenter: FeedsCommentPresenter

    var mFeedsID: Int? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var feedsCommendAdapter: FeedsCommentAdapter? = null

    init {
        inflate(context, com.module.feeds.R.layout.feeds_commont_view_layout, this)
        mRefreshLayout = findViewById(com.module.feeds.R.id.refreshLayout)
        mRecyclerView = findViewById(com.module.feeds.R.id.recycler_view)

        feedsCommendAdapter = FeedsCommentAdapter()
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.adapter = feedsCommendAdapter

        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true)
        mRefreshLayout.setEnableOverScrollDrag(false)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                mFeedsCommentPresenter.getFirstLevelCommentList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })
    }

    override fun noMore() {
        mRefreshLayout?.finishLoadMore()
        mRefreshLayout.setEnableLoadMore(false)
    }

    override fun updateList(list: List<FirstLevelCommentModel>?) {
        feedsCommendAdapter?.dataList = list
        mRefreshLayout?.finishLoadMore()
    }

    fun setFeedsID(feedsID: Int) {
        mFeedsID = feedsID
        mFeedsCommentPresenter = FeedsCommentPresenter(0, this)
        mFeedsCommentPresenter.getFirstLevelCommentList()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mFeedsCommentPresenter.destroy()
    }
}