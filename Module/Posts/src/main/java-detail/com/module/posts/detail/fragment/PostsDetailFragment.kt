package com.module.posts.detail.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.common.base.BaseFragment
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.posts.detail.adapter.PostsCommentAdapter
import com.module.posts.detail.adapter.PostsCommentAdapter.Companion.REFRESH_COMMENT_CTN
import com.module.posts.detail.inter.IPostsDetailView
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.detail.presenter.PostsDetailPresenter
import com.module.posts.detail.view.PostsInputContainerView
import com.module.posts.watch.model.PostsWatchModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout


class PostsDetailFragment : BaseFragment(), IPostsDetailView {
    lateinit var titlebar: CommonTitleBar
    lateinit var recyclerView: RecyclerView
    lateinit var commentTv: ExTextView
    lateinit var imageIv: ExImageView
    lateinit var audioIv: ExImageView
    lateinit var feedsInputContainerView: PostsInputContainerView
    lateinit var smartRefreshLayout: SmartRefreshLayout
    var mPostsWatchModel: PostsWatchModel? = null
    var mPostsDetailPresenter: PostsDetailPresenter? = null

    var postsAdapter: PostsCommentAdapter? = null

    override fun initView(): Int {
        return com.module.posts.R.layout.posts_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mPostsWatchModel == null) {
            activity?.finish()
            return
        }

        titlebar = rootView.findViewById(com.module.posts.R.id.titlebar)

        recyclerView = rootView.findViewById(com.module.posts.R.id.recycler_view)
        commentTv = rootView.findViewById(com.module.posts.R.id.comment_tv)
        imageIv = rootView.findViewById(com.module.posts.R.id.image_iv)
        audioIv = rootView.findViewById(com.module.posts.R.id.audio_iv)
        feedsInputContainerView = rootView.findViewById(com.module.posts.R.id.feeds_input_container_view)
        smartRefreshLayout.setEnableLoadMore(true)
        smartRefreshLayout.setEnableRefresh(false)

        titlebar.leftTextView.setDebounceViewClickListener {
            activity?.finish()
        }

        titlebar.rightTextView.setDebounceViewClickListener {

        }

        feedsInputContainerView?.mSendCallBack = { s ->

        }

        commentTv?.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput()
            feedsInputContainerView?.setETHint("回复")
        }

        mPostsDetailPresenter = PostsDetailPresenter(mPostsWatchModel!!.posts!!, this)
        addPresent(mPostsDetailPresenter)

        postsAdapter = PostsCommentAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = postsAdapter

        mPostsDetailPresenter?.getPostsFirstLevelCommentList()
    }

    override fun showFirstLevelCommentList(list: List<PostFirstLevelCommentModel>, hasMore: Boolean) {
        val modelList: MutableList<Any> = mutableListOf(mPostsWatchModel!!.posts!!, list)
        postsAdapter?.mCommentCtn = list.size
        postsAdapter?.dataList = modelList
        postsAdapter?.notifyItemChanged(0, REFRESH_COMMENT_CTN)
        smartRefreshLayout.setEnableLoadMore(hasMore)
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mPostsWatchModel = data as PostsWatchModel?
        }
    }

    override fun destroy() {
        super.destroy()
        val pool = recyclerView.recycledViewPool
        var holder: PostsCommentAdapter.PostsHolder? = pool.getRecycledView(0) as PostsCommentAdapter.PostsHolder?
        holder?.followTv?.destroy()
    }
}