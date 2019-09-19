package com.module.posts.detail.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.common.base.BaseFragment
import com.common.view.titlebar.CommonTitleBar
import com.module.posts.R
import com.module.posts.detail.adapter.PostsCommentDetailAdapter

class PostsCommentDetailFragment : BaseFragment() {
    var titlebar: CommonTitleBar? = null
    var recyclerView: RecyclerView? = null
    var postsAdapter: PostsCommentDetailAdapter? = null

    override fun initView(): Int {
        return R.layout.posts_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = rootView.findViewById(R.id.titlebar)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        postsAdapter = PostsCommentDetailAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = postsAdapter
    }
}