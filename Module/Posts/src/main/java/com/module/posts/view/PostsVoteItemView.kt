package com.module.posts.view

import android.view.View
import android.view.ViewStub
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.posts.R

class PostsVoteItemView(viewStub: ViewStub) : ExViewStub(viewStub) {
    lateinit var voteBgIv: ExImageView
    lateinit var voteProgress: ExImageView
    lateinit var desLeftTv: ExTextView
    lateinit var desCenterTv: ExTextView
    lateinit var voteNumTv: ExTextView

    override fun init(parentView: View) {
        voteBgIv = parentView.findViewById(R.id.vote_bg_iv)
        voteProgress = parentView.findViewById(R.id.vote_progress)
        desLeftTv = parentView.findViewById(R.id.des_left_tv)
        desCenterTv = parentView.findViewById(R.id.des_center_tv)
        voteNumTv = parentView.findViewById(R.id.voteNum_tv)
    }

    fun bindData(withAnim: Boolean) {
        tryInflate()

    }

    override fun layoutDesc(): Int {
        return R.layout.post_vote_item_layout
    }
}