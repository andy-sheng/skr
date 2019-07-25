package com.component.feeds.holder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.busilib.view.BitmapTextView
import com.component.feeds.adapter.FeedsWatchViewAdapter
import com.component.feeds.listener.FeedsListener
import com.component.feeds.model.FeedsWatchModel
import com.component.feeds.view.RecordAnimationView
import com.facebook.drawee.view.SimpleDraweeView

open class FeedsWatchViewHolder(var item: View, var listener: FeedsListener?) : RecyclerView.ViewHolder(item) {

    val mAvatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
    private val mNicknameTv: TextView = itemView.findViewById(R.id.nickname_tv)
    private val mTimeTv: TextView = itemView.findViewById(R.id.time_tv)
    private val mContentTv: TextView = itemView.findViewById(R.id.content_tv)
    private val mPeopleNumTv: BitmapTextView = itemView.findViewById(R.id.people_num_tv)
    private val mHitIv: ImageView = itemView.findViewById(R.id.hit_iv)

    private val mMoreIv: ImageView = itemView.findViewById(R.id.more_iv)
    private val mTagArea: ExConstraintLayout = itemView.findViewById(R.id.tag_area)
    val mTagIv: ImageView = itemView.findViewById(R.id.tag_iv)
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

        mHitIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickHitListener(model)
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

    fun bindData(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        if (watchModel != model) {
            this.model = watchModel

            mTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model?.song?.createdAt!!, System.currentTimeMillis())
            model?.user?.let {
                AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(it.avatar)
                        .setCircle(true)
                        .build())
                mNicknameTv.text = UserInfoManager.getInstance().getRemarkName(it.userID, it.nickname)

                AvatarUtils.loadAvatarByUrl(mSongAreaBg, AvatarUtils.newParamsBuilder(it.avatar)
                        .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                        .setBlur(true)
                        .build())
                mRecordView.bindData(it.avatar)
            }
            mPeopleNumTv.setText(watchModel.challengeCnt.toString())
            mLikeNumTv.text = watchModel.starCnt.toString()
            mCommentNumTv.text = watchModel.commentCnt.toString()
            if (!TextUtils.isEmpty(watchModel.song?.title)) {
                mContentTv.text = watchModel.song?.title
                mContentTv.visibility = View.VISIBLE
            } else {
                mContentTv.visibility = View.GONE
            }

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