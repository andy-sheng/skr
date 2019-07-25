package com.module.feeds.detail.view

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.alibaba.android.arouter.launcher.ARouter
import com.common.view.ex.ExConstraintLayout
import com.component.feeds.model.FeedsWatchModel
import com.module.RouterConstants
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
    var mFeedsCommentPresenter: FeedsCommentPresenter? = null

    var feedsCommendAdapter: FeedsCommentAdapter

    var mFeedsID: Int? = null

    var mFeedsWatchModel: FeedsWatchModel? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, com.module.feeds.R.layout.feeds_commont_view_layout, this)
        mRefreshLayout = findViewById(com.module.feeds.R.id.refreshLayout)
        mRecyclerView = findViewById(com.module.feeds.R.id.recycler_view)

        feedsCommendAdapter = FeedsCommentAdapter()
        feedsCommendAdapter.mIFirstLevelCommentListener = object : FeedsCommentAdapter.IFirstLevelCommentListener {
            override fun onClickLike(firstLevelCommentModel: FirstLevelCommentModel, like: Boolean, position: Int) {
                mFeedsCommentPresenter?.likeComment(firstLevelCommentModel, mFeedsID!!, like, position)
            }

            override fun onClickMoreComment(firstLevelCommentModel: FirstLevelCommentModel) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_SECOND_DETAIL)
                        .withSerializable("feed_model", mFeedsWatchModel)
                        .navigation()
            }

            override fun onClickIcon(userID: Int) {
                val bundle = Bundle()
                bundle.putInt("bundle_user_id", userID)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                        .with(bundle)
                        .navigation()
            }
        }

        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.adapter = feedsCommendAdapter

        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true)
        mRefreshLayout.setEnableOverScrollDrag(false)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                mFeedsCommentPresenter?.getFirstLevelCommentList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })
    }

    fun addSelfComment(model: FirstLevelCommentModel) {
        mFeedsCommentPresenter?.mModelList?.add(0, model)
        mFeedsCommentPresenter?.mOffset?.let {
            it.plus(1)
        }

        feedsCommendAdapter.dataList.add(0, model)
        feedsCommendAdapter.notifyDataSetChanged()
    }

    override fun noMore() {
        mRefreshLayout?.finishLoadMore()
        mRefreshLayout.setEnableLoadMore(false)
    }

    override fun likeFinish(firstLevelCommentModel: FirstLevelCommentModel, position: Int) {
        feedsCommendAdapter?.notifyItemChanged(position)
    }

    override fun updateList(list: List<FirstLevelCommentModel>?) {
        feedsCommendAdapter?.dataList = list
        mRefreshLayout?.finishLoadMore()
    }

    fun setFeedsID(feedsWatchModel: FeedsWatchModel) {
        mFeedsID = feedsWatchModel.feedID
        mFeedsWatchModel = feedsWatchModel
        mFeedsCommentPresenter = FeedsCommentPresenter(mFeedsID!!, this)
        mFeedsCommentPresenter?.getFirstLevelCommentList()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mFeedsCommentPresenter?.destroy()
    }
}