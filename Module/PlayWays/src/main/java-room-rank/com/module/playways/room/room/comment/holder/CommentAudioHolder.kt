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
import com.module.playways.room.room.comment.listener.CommentItemListener
import com.module.playways.room.room.comment.model.CommentAudioModel

class CommentAudioHolder(itemView: View, listener: CommentItemListener?) : RecyclerView.ViewHolder(itemView) {

    internal var mAvatarIv: BaseImageView
    internal var mNameTv: ExTextView
    internal var mAudioTv: ExTextView
    internal var mAudioPlayIv: ImageView
    internal var mRedIv: ExImageView

    internal var position: Int = 0
    internal var mCommentAudioModel: CommentAudioModel? = null

    init {
        mAvatarIv = itemView.findViewById(R.id.avatar_iv)
        mNameTv = itemView.findViewById(R.id.name_tv)
        mAudioTv = itemView.findViewById(R.id.audio_tv)
        mAudioPlayIv = itemView.findViewById(R.id.audio_play_iv)
        mRedIv = itemView.findViewById(R.id.red_iv)

        mAvatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                listener?.clickAvatar(mCommentAudioModel!!.userId)
            }
        })
    }

    fun bind(position: Int, model: CommentAudioModel) {
        this.position = position
        this.mCommentAudioModel = model

        if (mCommentAudioModel!!.isRead) {
            mRedIv.visibility = View.GONE
        } else {
            mRedIv.visibility = View.VISIBLE
        }
        mNameTv.text = model.stringBuilder
        val duration = Math.ceil((model.duration / 1000).toDouble())
        mAudioTv.text = duration.toInt().toString() + "s"
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(model.avatar)
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setBorderColor(model.avatarColor)
                .build())
    }
}
