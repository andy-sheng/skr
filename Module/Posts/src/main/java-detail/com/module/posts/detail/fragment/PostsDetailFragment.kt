package com.module.posts.detail.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.base.BaseFragment
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.posts.R
import com.module.posts.detail.adapter.PostsCommentAdapter
import com.module.posts.detail.adapter.PostsCommentAdapter.Companion.DESTROY_HOLDER
import com.module.posts.detail.adapter.PostsCommentAdapter.Companion.REFRESH_COMMENT_CTN
import com.module.posts.detail.inter.IPostsDetailView
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.detail.presenter.PostsDetailPresenter
import com.module.posts.detail.view.PostsInputContainerView
import com.module.posts.watch.model.PostsWatchModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
        return R.layout.posts_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mPostsWatchModel == null) {
            activity?.finish()
            return
        }

        titlebar = rootView.findViewById(R.id.titlebar)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        commentTv = rootView.findViewById(R.id.comment_tv)
        imageIv = rootView.findViewById(R.id.image_iv)
        audioIv = rootView.findViewById(R.id.audio_iv)
        feedsInputContainerView = rootView.findViewById(R.id.feeds_input_container_view)
        smartRefreshLayout = rootView.findViewById(R.id.smart_refresh)
        smartRefreshLayout.setEnableLoadMore(true)
        smartRefreshLayout.setEnableRefresh(false)

        smartRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                mPostsDetailPresenter?.getPostsFirstLevelCommentList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })

        titlebar.leftTextView.setDebounceViewClickListener {
            activity?.finish()
        }

        titlebar.rightImageButton.setDebounceViewClickListener {

        }

        feedsInputContainerView?.mSendCallBack = { s ->

        }

        commentTv?.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD)
            feedsInputContainerView?.setETHint("回复")
        }

        imageIv.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.IMG)
            feedsInputContainerView?.setETHint("回复")
        }

        audioIv.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.AUDIO)
            feedsInputContainerView?.setETHint("回复")
        }

        mPostsDetailPresenter = PostsDetailPresenter(mPostsWatchModel!!.posts!!, this)
        addPresent(mPostsDetailPresenter)

        postsAdapter = PostsCommentAdapter()
        postsAdapter?.mIDetailClickListener = object : PostsCommentAdapter.IDetailClickListener {
            override fun replayPosts() {

            }

            override fun likePosts(model: PostsWatchModel) {
                mPostsDetailPresenter?.likePosts(!model.isLiked!!, model)
            }

            override fun clickFirstLevelComment() {

            }

            override fun likeFirstLevelComment(model: PostFirstLevelCommentModel) {
                mPostsDetailPresenter?.likeFirstLevelComment(!model.isIsLiked!!, model)
            }
        }

        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = postsAdapter

        mPostsDetailPresenter?.getPostsFirstLevelCommentList()
    }

    override fun showFirstLevelCommentList(list: List<PostFirstLevelCommentModel>, hasMore: Boolean) {
        val modelList: MutableList<Any> = mutableListOf(mPostsWatchModel!!)
        modelList.addAll(list)
        postsAdapter?.mCommentCtn = list.size
        postsAdapter?.dataList = modelList
        launch {
            delay(10)
            postsAdapter?.notifyItemChanged(0, REFRESH_COMMENT_CTN)
        }
        smartRefreshLayout.setEnableLoadMore(hasMore)
        smartRefreshLayout.finishLoadMore()
    }

    override fun showLikePostsResulet() {
        postsAdapter?.notifyDataSetChanged()
    }

    override fun showLikeFirstLevelCommentResult(postFirstLevelCommentModel: PostFirstLevelCommentModel) {
        postsAdapter?.notifyDataSetChanged()
    }

    override fun loadMoreError() {
        smartRefreshLayout.finishLoadMore()
    }

    override fun isBlackStatusBarText(): Boolean = true

    override fun useEventBus(): Boolean {
        return false
    }

    override fun onBackPressed(): Boolean {
        if(feedsInputContainerView.mInputContainer?.visibility == View.VISIBLE){
            feedsInputContainerView.hideSoftInput()
            return true
        }

        return super.onBackPressed()
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mPostsWatchModel = data as PostsWatchModel?
        }
    }

    override fun destroy() {
        super.destroy()
        postsAdapter?.notifyItemChanged(0, DESTROY_HOLDER)
    }
}