package com.module.feeds.detail.view

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.alibaba.android.arouter.launcher.ARouter
import com.common.view.ex.ExConstraintLayout
import com.module.RouterConstants
import com.module.feeds.detail.adapter.FeedsCommentAdapter
import com.module.feeds.detail.inter.IFirstLevelCommentView
import com.module.feeds.detail.model.CommentCountModel
import com.module.feeds.detail.model.FirstLevelCommentModel
import com.module.feeds.detail.presenter.FeedsCommentPresenter
import com.module.feeds.watch.model.FeedsWatchModel
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

        feedsCommendAdapter = FeedsCommentAdapter(false)
        feedsCommendAdapter.mIFirstLevelCommentListener = object : FeedsCommentAdapter.IFirstLevelCommentListener {
            override fun onClickLike(firstLevelCommentModel: FirstLevelCommentModel, like: Boolean, position: Int) {
                mFeedsCommentPresenter?.likeComment(firstLevelCommentModel, mFeedsID!!, like, position)
            }

            override fun onClickContent(firstLevelCommentModel: FirstLevelCommentModel) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_SECOND_DETAIL)
                        .withSerializable("comment_model", firstLevelCommentModel)
                        .withInt("feed_id", mFeedsID!!)
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
        mFeedsCommentPresenter?.mOffset = mFeedsCommentPresenter?.mOffset!! + 1

        feedsCommendAdapter.dataList.add(1, model)
        feedsCommendAdapter.notifyDataSetChanged()
    }


    override fun noMore() {
        mRefreshLayout?.finishLoadMore()
        mRefreshLayout.setEnableLoadMore(false)
    }

    override fun likeFinish(firstLevelCommentModel: FirstLevelCommentModel, position: Int, like: Boolean) {
        feedsCommendAdapter?.notifyItemChanged(position)
    }

    override fun finishLoadMore() {
        mRefreshLayout?.finishLoadMore()
    }

    override fun updateList(list: List<FirstLevelCommentModel>?) {
        val mList: ArrayList<Any> = ArrayList(list)
        mList.add(0, CommentCountModel(509))
        feedsCommendAdapter?.dataList = mList
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