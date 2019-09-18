package com.module.posts.watch.view

import android.view.View
import android.view.ViewStub
import com.common.view.ExViewStub
import com.module.posts.R

class PostsWatchCommentView(viewStub: ViewStub) :ExViewStub(viewStub){
    override fun init(parentView: View) {

    }

    override fun layoutDesc(): Int {
        return R.layout.posts_watch_view_item_comment_stub_layout
    }

}