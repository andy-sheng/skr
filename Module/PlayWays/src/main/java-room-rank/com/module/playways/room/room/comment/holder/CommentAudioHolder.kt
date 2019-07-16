package com.module.playways.room.room.comment.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView

import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.room.room.comment.adapter.CommentAdapter
import com.module.playways.room.room.comment.model.CommentAudioModel
import com.module.playways.grab.room.top.SpeakingTipsAnimationView

class CommentAudioHolder(itemView: View, listener: CommentAdapter.CommentAdapterListener?) : RecyclerView.ViewHolder(itemView) {

    internal var mAvatarIv: BaseImageView
    internal var mNameTv: ExTextView
    internal var mAudioTv: ExTextView
    internal var mAudioPlayIv: ImageView
    internal var mRedIv: ExImageView
    internal var mSpeakerAnimationIv: SpeakingTipsAnimationView

    internal var position: Int = 0
    internal var mCommentAudioModel: CommentAudioModel? = null
    var isPlaying = false

    val minSize = U.getDisplayUtils().dip2px(66f)    // 最小尺寸(小于5秒)
    val maxSize = U.getDisplayUtils().dip2px(100f)   // 最大尺寸(大于10秒）

    init {
        mAvatarIv = itemView.findViewById(R.id.avatar_iv)
        mNameTv = itemView.findViewById(R.id.name_tv)
        mAudioTv = itemView.findViewById(R.id.audio_tv)
        mAudioPlayIv = itemView.findViewById(R.id.audio_play_iv)
        mRedIv = itemView.findViewById(R.id.red_iv)
        mSpeakerAnimationIv = itemView.findViewById(R.id.speaker_animation_iv)

        mAvatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                listener?.clickAvatar(mCommentAudioModel!!.userId)
            }
        })

        mAudioTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mCommentAudioModel?.isRead = true
                mRedIv.visibility = View.GONE
                listener?.clickAudio(isPlaying, mCommentAudioModel)
            }
        })
    }

    fun bind(position: Int, model: CommentAudioModel) {
        this.position = position
        this.mCommentAudioModel = model

        val duration = Math.ceil((model.duration / 1000).toDouble())

        val width = when (duration.toInt()) {
            in 0..5 -> minSize
            in 6..10 -> minSize + (maxSize - minSize) / 5 * (duration.toInt() - 5)
            else -> maxSize
        }
        var lp = mAudioTv.layoutParams
        lp.width = width
        mAudioTv.layoutParams = lp

        if (mCommentAudioModel!!.isRead) {
            mRedIv.visibility = View.GONE
        } else {
            mRedIv.visibility = View.VISIBLE
        }
        mNameTv.text = model.stringBuilder
        mAudioTv.text = duration.toInt().toString() + "s"
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(model.avatar)
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setBorderColor(model.avatarColor)
                .build())
    }


    fun setPlay(isPlay: Boolean) {
        isPlaying = isPlay
        if (isPlay) {
            // 播放动画
            mSpeakerAnimationIv.show((mCommentAudioModel?.duration ?: 0).toInt())
        } else {
            // 停止动画
            mSpeakerAnimationIv.hide()
        }
    }
}
