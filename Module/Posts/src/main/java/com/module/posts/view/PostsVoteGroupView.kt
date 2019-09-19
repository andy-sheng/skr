package com.module.posts.view

import android.view.View
import android.view.ViewStub
import com.common.view.ExViewStub
import com.module.posts.R

class PostsVoteGroupView(viewStub: ViewStub) : ExViewStub(viewStub) {
    lateinit var voteItem1: PostsVoteItemView
    lateinit var voteItem2: PostsVoteItemView
    lateinit var voteItem3: PostsVoteItemView
    lateinit var voteItem4: PostsVoteItemView
    lateinit var viewList: MutableList<PostsVoteItemView>

    override fun init(parentView: View) {
        voteItem1 = PostsVoteItemView(parentView.findViewById(R.id.vote_item_1))
        voteItem2 = PostsVoteItemView(parentView.findViewById(R.id.vote_item_2))
        voteItem3 = PostsVoteItemView(parentView.findViewById(R.id.vote_item_3))
        voteItem4 = PostsVoteItemView(parentView.findViewById(R.id.vote_item_4))
        viewList = mutableListOf(voteItem1, voteItem2, voteItem3, voteItem4)
    }

    fun bindData(withAnim: Boolean) {
        tryInflate()
        voteItem1.setVisibility(View.GONE)
        voteItem2.setVisibility(View.GONE)
        voteItem3.setVisibility(View.GONE)
        voteItem4.setVisibility(View.GONE)

    }

    override fun layoutDesc(): Int {
        return R.layout.post_vote_view_layout
    }
}