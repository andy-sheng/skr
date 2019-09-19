package com.module.posts.view

import android.graphics.Color
import android.view.View
import android.view.ViewStub
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.ex.drawable.DrawableFactory
import com.module.posts.R
import com.module.posts.watch.model.PostsVoteItemModel
import com.module.posts.watch.model.PostsVoteModel

class PostsVoteItemView(viewStub: ViewStub) : ExViewStub(viewStub) {
    lateinit var voteBgIv: ExImageView
    lateinit var voteProgress: ExImageView
    lateinit var desLeftTv: ExTextView
    lateinit var desCenterTv: ExTextView
    lateinit var voteNumTv: ExTextView

    var clickListener: ((index: Int) -> Unit)? = null

    val voteDrawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#19006DFF"))
            .setCornersRadius(8.dp().toFloat(), 8.dp().toFloat(), 8.dp().toFloat(), 8.dp().toFloat())
            .build()
    val voteDrawableSelf = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#F8D1FF"))
            .setCornersRadius(8.dp().toFloat(), 8.dp().toFloat(), 8.dp().toFloat(), 8.dp().toFloat())
            .build()

    var mPos = -1

    override fun init(parentView: View) {
        voteBgIv = parentView.findViewById(R.id.vote_bg_iv)
        voteProgress = parentView.findViewById(R.id.vote_progress)
        desLeftTv = parentView.findViewById(R.id.des_left_tv)
        desCenterTv = parentView.findViewById(R.id.des_center_tv)
        voteNumTv = parentView.findViewById(R.id.voteNum_tv)

        voteBgIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                clickListener?.invoke(mPos + 1)
            }
        })
    }

    fun bindData(index: Int, voteItem: PostsVoteItemModel, model: PostsVoteModel) {
        tryInflate()

        setVisibility(View.VISIBLE)
        mPos = index
        if (model.hasVoted == true) {
            // 已经投票了
            desLeftTv.text = voteItem.voteItem
            voteNumTv.text = "${voteItem.voteCnt}票"
            var totalVotes = 0
            model.voteList?.forEach {
                totalVotes += it.voteCnt.toInt()
            }
            val layoutParams = voteProgress.layoutParams
            layoutParams.width = ((U.getDisplayUtils().screenWidth - 40.dp()) * voteItem.voteCnt / totalVotes).toInt()
            voteProgress.layoutParams = layoutParams
            if ((index + 1) == model.voteSeq) {
                voteProgress.background = voteDrawableSelf
            } else {
                voteProgress.background = voteDrawable
            }
            voteProgress.visibility = View.VISIBLE
            desLeftTv.visibility = View.VISIBLE
            voteNumTv.visibility = View.VISIBLE
            desCenterTv.visibility = View.GONE
        } else {
            // 未投票
            desCenterTv.text = voteItem.voteItem
            desCenterTv.visibility = View.VISIBLE
            voteProgress.visibility = View.GONE
            desLeftTv.visibility = View.GONE
            voteNumTv.visibility = View.GONE
        }

    }

    override fun layoutDesc(): Int {
        return R.layout.post_vote_item_layout
    }
}