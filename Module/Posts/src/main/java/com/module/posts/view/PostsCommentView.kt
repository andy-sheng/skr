package com.module.posts.view

import android.view.View
import android.view.ViewStub
import com.common.view.ExViewStub
import com.module.posts.R

class PostsCommentView(viewStub: ViewStub) : ExViewStub(viewStub) {

    override fun init(parentView: View) {

    }

    override fun layoutDesc(): Int {
        return R.layout.post_comment_view_layout
    }

    fun bindData() {
        tryInflate()
    }
}