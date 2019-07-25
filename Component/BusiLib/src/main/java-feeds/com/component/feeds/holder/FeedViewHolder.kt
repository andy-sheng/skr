package com.component.feeds.holder

import android.support.v7.widget.RecyclerView
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
    private val mClassifySongTv: ExTextView = itemView.findViewById(R.id.classify_song_tv)
    private val mSongAreaBg: SimpleDraweeView = itemView.findViewById(R.id.song_area_bg)
    private val mRecordView: RecordAnimationView = itemView.findViewById(R.id.record_view)
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

        itemView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickDetailListener(model)
            }
        })
    }

    open fun bindData(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        if (watchModel != model) {
            this.model = watchModel

            model?.user?.let {
                AvatarUtils.loadAvatarByUrl(mSongAreaBg, AvatarUtils.newParamsBuilder(it.avatar)
                        .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                        .setBlur(true)
                        .build())
                mRecordView.bindData(it.avatar)
            }
            mLikeNumTv.text = watchModel.starCnt.toString()
            mCommentNumTv.text = watchModel.commentCnt.toString()

            if (model?.rank != null) {
                model?.rank?.let {
                    when {
                        it.rankType == 1 -> {
                            mTagArea.visibility = View.VISIBLE
                            mTagTv.text = it.rankDesc
                            mClassifySongTv.visibility = View.VISIBLE
                        }
                        it.rankType == 2 -> {
                            mTagArea.visibility = View.VISIBLE
                            mTagTv.text = it.rankDesc
                            mClassifySongTv.visibility = View.GONE
                            // 展示分类
                        }
                        else -> {
                            mTagArea.visibility = View.GONE
                            mClassifySongTv.visibility = View.GONE
                        }
                    }
                }
            } else {
                mTagArea.visibility = View.GONE
                mClassifySongTv.visibility = View.GONE
            }
        } else {
            // do noting
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