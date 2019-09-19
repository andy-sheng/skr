package com.module.posts.detail.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.common.base.BaseFragment
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.posts.detail.adapter.PostsCommentDetailAdapter
import com.module.posts.detail.inter.IPostsCommentDetailView
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.detail.model.PostsSecondLevelCommentModel
import com.module.posts.detail.presenter.PostsCommentDetailPresenter
import com.module.posts.detail.view.PostsInputContainerView
import com.module.posts.watch.model.PostsWatchModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout


class PostsCommentDetailFragment : BaseFragment(), IPostsCommentDetailView {
    lateinit var titlebar: CommonTitleBar
    lateinit var recyclerView: RecyclerView
    lateinit var commentTv: ExTextView
    lateinit var audioIv: ExImageView
    lateinit var feedsInputContainerView: PostsInputContainerView
    lateinit var smartRefreshLayout: SmartRefreshLayout
    var mPostsWatchModel: PostsWatchModel? = null
    var mPostFirstLevelCommentModel: PostFirstLevelCommentModel? = null
    var postsAdapter: PostsCommentDetailAdapter? = null

    var postsCommentDetailPresenter: PostsCommentDetailPresenter? = null

    override fun initView(): Int {
        return com.module.posts.R.layout.posts_comment_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mPostsWatchModel == null || mPostFirstLevelCommentModel == null) {
            activity?.finish()
            return
        }

        titlebar = rootView.findViewById(com.module.posts.R.id.titlebar)
        recyclerView = rootView.findViewById(com.module.posts.R.id.recycler_view)
        commentTv = rootView.findViewById(com.module.posts.R.id.comment_tv)
        audioIv = rootView.findViewById(com.module.posts.R.id.audio_iv)
        feedsInputContainerView = rootView.findViewById(com.module.posts.R.id.feeds_input_container_view)
        smartRefreshLayout = rootView.findViewById(com.module.posts.R.id.smart_refresh)
        smartRefreshLayout.setEnableLoadMore(true)
        smartRefreshLayout.setEnableRefresh(false)

        postsCommentDetailPresenter = PostsCommentDetailPresenter(mPostsWatchModel!!.posts!!, this)

        postsAdapter = PostsCommentDetailAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = postsAdapter

        postsCommentDetailPresenter?.getPostsSecondLevelCommentList()
    }

    override fun showSecondLevelCommentList(list: List<PostsSecondLevelCommentModel>, hasMore: Boolean) {
        val modelList: MutableList<Any> = mutableListOf(mPostFirstLevelCommentModel!!, list)
        postsAdapter?.dataList = modelList
        smartRefreshLayout.setEnableLoadMore(hasMore)
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mPostsWatchModel = data as PostsWatchModel?
        } else if (type == 1) {
            mPostFirstLevelCommentModel = data as PostFirstLevelCommentModel?
        }
    }

    override fun destroy() {
        super.destroy()
        val pool = recyclerView.recycledViewPool
        var holder: PostsCommentDetailAdapter.PostsFirstLevelCommentHolder? = pool.getRecycledView(0) as PostsCommentDetailAdapter.PostsFirstLevelCommentHolder?
        holder?.followTv?.destroy()
    }
}