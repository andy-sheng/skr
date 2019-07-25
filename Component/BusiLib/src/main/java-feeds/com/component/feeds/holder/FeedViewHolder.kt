package com.component.feeds.holder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.feeds.listener.FeedsListener
import com.component.feeds.model.FeedsWatchModel
import com.component.feeds.view.RecordAnimationView
import com.facebook.drawee.view.SimpleDraweeView

open abstract class FeedViewHolder(var item: View, var listener: FeedsListener?) : RecyclerView.ViewHolder(item) {

    private val mMoreIv: ImageView = itemView.findViewById(R.id.more_iv)
    private val mTagArea: ExConstraintLayout = itemView.findViewById(R.id.tag_area)
    private val mTagTv: TextView = itemView.findViewById(R.id.tag_tv)
    //    private val mClassifySongTv: ExTextView = itemView.findViewById(R.id.classify_song_tv)
    val mSongAreaBg: SimpleDraweeView = itemView.findViewById(R.id.song_area_bg)
    val mRecordView: RecordAnimationView = itemView.findViewById(R.id.record_view)
    private val mLikeNumTv: TextView = itemView.findViewById(R.id.like_num_tv)
    private val mCommentNumTv: TextView = itemView.findViewById(R.id.comment_num_tv)

    var mPosition: Int = 0
    var model: FeedsWatchModel? = null
    var mIsPlaying = false

    init {
        mMoreIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickMoreListener(model)
            }
        })

        mLikeNumTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickLikeListener(model)
            }
        })

        mCommentNumTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickCommentListener(model)
            }
        })

        mRecordView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickCDListener(model)
            }
        })

        mTagArea.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onclickRankListener(model)
            }
        })

        itemView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickDetailListener(model)
            }
        })
    }

    open fun bindData(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        if (watchModel.feedID != model?.feedID) {
            // 全部初始化
            mLikeNumTv.text = watchModel.starCnt.toString()
            mCommentNumTv.text = watchModel.commentCnt.toString()

            if (watchModel?.isLiked == true) {
                mLikeNumTv.setCompoundDrawables(U.getDrawable(R.drawable.feed_like_selected_icon), null, null, null)
            } else {
                mLikeNumTv.setCompoundDrawables(U.getDrawable(R.drawable.feed_like_normal_icon), null, null, null)
            }
            if (watchModel?.rank != null) {
                if (TextUtils.isEmpty(watchModel?.rank?.rankDesc)) {
                    mTagArea.visibility = View.GONE
                } else {
                    mTagTv.text = watchModel?.rank?.rankDesc
                    mTagArea.visibility = View.VISIBLE
                }
            } else {
                mTagArea.visibility = View.GONE
            }
        } else {
            // do noting (对变化的部分重新设置 评论数，点赞数和点赞icon)
            if (model?.starCnt != watchModel.starCnt) {
                mLikeNumTv.text = watchModel.starCnt.toString()
            }
            if (model?.commentCnt != watchModel.commentCnt) {
                mCommentNumTv.text = watchModel.commentCnt.toString()
            }
            if (model?.isLiked != watchModel?.isLiked) {
                if (watchModel?.isLiked == true) {
                    mLikeNumTv.setCompoundDrawables(U.getDrawable(R.drawable.feed_like_selected_icon), null, null, null)
                } else {
                    mLikeNumTv.setCompoundDrawables(U.getDrawable(R.drawable.feed_like_normal_icon), null, null, null)
                }
            }
        }
    }

    fun startPlay() {
        mIsPlaying = true
        mRecordView.startAnimation()
    }

    fun stopPlay() {
        mRecordView.stopAnimation(mIsPlaying)
        mIsPlaying = false
    }
}