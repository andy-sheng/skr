package com.module.posts.detail.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.common.base.BaseFragment
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.posts.R
import com.module.posts.detail.adapter.PostsCommentDetailAdapter
import com.module.posts.detail.view.PostsInputContainerView

class PostsCommentDetailFragment : BaseFragment() {
    lateinit var titlebar: CommonTitleBar
    lateinit var recyclerView: RecyclerView
    lateinit var commentTv: ExTextView
    lateinit var audioIv: ExImageView
    lateinit var feedsInputContainerView: PostsInputContainerView
    var postsAdapter: PostsCommentDetailAdapter? = null

    override fun initView(): Int {
        return R.layout.posts_comment_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = rootView.findViewById(R.id.titlebar)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        commentTv = rootView.findViewById(R.id.comment_tv)
        audioIv = rootView.findViewById(R.id.audio_iv)
        feedsInputContainerView = rootView.findViewById(R.id.feeds_input_container_view)

        postsAdapter = PostsCommentDetailAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = postsAdapter
    }
}