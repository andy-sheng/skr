package com.module.feeds.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.module.feeds.watch.view.RecordAnimationView
import com.facebook.drawee.view.SimpleDraweeView
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.component.busilib.view.BitmapTextView
import com.module.feeds.R
import com.module.feeds.watch.model.FeedsWatchModel

class FeedsWatchViewHolder(item: View,
                           var onClickMoreListener: ((watchModel: FeedsWatchModel?) -> Unit)?,
                           var onClickLikeListener: ((watchModel: FeedsWatchModel?) -> Unit)?,
                           var onClickCommentListener: ((watchModel: FeedsWatchModel?) -> Unit)?,
                           var onClickHitListener: ((watchModel: FeedsWatchModel?) -> Unit)?,
                           var onClickCDListener: ((watchModel: FeedsWatchModel?) -> Unit)?,
                           var onClickDetailListener: ((watchModel: FeedsWatchModel?) -> Unit)?) : RecyclerView.ViewHolder(item) {

    val mAvatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
    val mNicknameTv: TextView = itemView.findViewById(R.id.nickname_tv)
    val mTimeTv: TextView = itemView.findViewById(R.id.time_tv)
    val mMoreIv: ImageView = itemView.findViewById(R.id.more_iv)
    val mContentTv: TextView = itemView.findViewById(R.id.content_tv)
    val mTagArea: ExConstraintLayout = itemView.findViewById(R.id.tag_area)
    val mTagIv: ImageView = itemView.findViewById(R.id.tag_iv)
    val mTagTv: TextView = itemView.findViewById(R.id.tag_tv)
    val mClassifySongTv: ExTextView = itemView.findViewById(R.id.classify_song_tv)
    val mSongAreaBg: SimpleDraweeView = itemView.findViewById(R.id.song_area_bg)
    val mRecordView: RecordAnimationView = itemView.findViewById(R.id.record_view)
    val mLikeNumTv: TextView = itemView.findViewById(R.id.like_num_tv)
    val mCommentNumTv: TextView = itemView.findViewById(R.id.comment_num_tv)

    val mHitIv: ImageView = itemView.findViewById(R.id.hit_iv)
    val mPeopleNumTv: BitmapTextView = itemView.findViewById(R.id.people_num_tv)

    var mPosition: Int = 0
    var model: FeedsWatchModel? = null
    var mIsPlaying = false

    init {
        mMoreIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickMoreListener?.invoke(model)
            }
        })

        mLikeNumTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickLikeListener?.invoke(model)
            }
        })

        mCommentNumTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickCommentListener?.invoke(model)
            }
        })

        mHitIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickHitListener?.invoke(model)
            }
        })

        mRecordView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickCDListener?.invoke(model)
            }
        })

        itemView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickDetailListener?.invoke(model)
            }
        })
    }

    fun bindData(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        if (watchModel != model) {
            this.model = watchModel

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