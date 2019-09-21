package com.module.posts.detail.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.common.base.BaseFragment
import com.common.core.view.setDebounceViewClickListener
import com.common.player.SinglePlayer
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.posts.detail.adapter.PostsCommentDetailAdapter
import com.module.posts.detail.adapter.PostsCommentDetailAdapter.Companion.DESTROY_HOLDER
import com.module.posts.detail.adapter.PostsCommentDetailAdapter.Companion.REFRESH_COMMENT_CTN
import com.module.posts.detail.inter.IPostsCommentDetailView
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.detail.model.PostsSecondLevelCommentModel
import com.module.posts.detail.presenter.PostsCommentDetailPresenter
import com.module.posts.detail.view.PostsInputContainerView
import com.module.posts.watch.model.PostsWatchModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch


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

        smartRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                postsCommentDetailPresenter?.getPostsSecondLevelCommentList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })

        titlebar.leftTextView.setDebounceViewClickListener {
            activity?.finish()
        }

        postsCommentDetailPresenter = PostsCommentDetailPresenter(mPostsWatchModel!!.posts!!, this)

        postsAdapter = PostsCommentDetailAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = postsAdapter

        postsCommentDetailPresenter?.getPostsSecondLevelCommentList()
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun showSecondLevelCommentList(list: List<PostsSecondLevelCommentModel>, hasMore: Boolean) {
        val modelList: MutableList<Any> = mutableListOf(mPostFirstLevelCommentModel!!)
        modelList.addAll(list)
        postsAdapter?.mCommentCtn = list.size
        postsAdapter?.dataList = modelList
        launch {
            kotlinx.coroutines.delay(10)
            postsAdapter?.notifyItemChanged(0, REFRESH_COMMENT_CTN)
        }
        smartRefreshLayout.setEnableLoadMore(hasMore)
        smartRefreshLayout.finishLoadMore()
    }

    override fun loadMoreError() {
        smartRefreshLayout.finishLoadMore()
    }

    override fun isBlackStatusBarText(): Boolean = true

    override fun setData(type: Int, data: Any?) {
        if (type == 1) {
            mPostsWatchModel = data as PostsWatchModel?
        } else if (type == 0) {
            mPostFirstLevelCommentModel = data as PostFirstLevelCommentModel?
        }
    }

    override fun onPause() {
        super.onPause()
        SinglePlayer.stop(PostsCommentDetailAdapter.playerTag)
    }

    override fun destroy() {
        super.destroy()
        postsAdapter?.notifyItemChanged(0, DESTROY_HOLDER)
        SinglePlayer.removeCallback(PostsCommentDetailAdapter.playerTag)
    }
}