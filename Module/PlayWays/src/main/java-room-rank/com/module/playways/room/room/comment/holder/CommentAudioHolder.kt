package com.module.playways.room.room.comment.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView

import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.UserLevelType
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.component.level.utils.LevelConfigUtils
import com.module.playways.R
import com.module.playways.room.room.comment.adapter.CommentAdapter
import com.module.playways.room.room.comment.model.CommentAudioModel
import com.module.playways.grab.room.top.SpeakingTipsAnimationView

class CommentAudioHolder(itemView: View, listener: CommentAdapter.CommentAdapterListener?) : RecyclerView.ViewHolder(itemView) {

    private val mAvatarIv: AvatarView = itemView.findViewById(R.id.avatar_iv)
    private val mNameTv: ExTextView = itemView.findViewById(R.id.name_tv)
    private val mAudioTv: ExTextView = itemView.findViewById(R.id.audio_tv)
    private val mAudioPlayIv: ImageView = itemView.findViewById(R.id.audio_play_iv)
    private val mRedIv: ExImageView = itemView.findViewById(R.id.red_iv)
    private val mSpeakerAnimationIv: SpeakingTipsAnimationView = itemView.findViewById(R.id.speaker_animation_iv)

    internal var position: Int = 0
    internal var mCommentAudioModel: CommentAudioModel? = null
    var isPlaying = false

    val minSize = U.getDisplayUtils().dip2px(66f)    // 最小尺寸(小于5秒)
    val maxSize = U.getDisplayUtils().dip2px(100f)   // 最大尺寸(大于10秒）

    init {
        mAvatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mCommentAudioModel?.let {
                    listener?.clickAvatar(it.userInfo.userId)
                }
            }
        })

        mAudioTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (listener?.clickAudio(isPlaying, mCommentAudioModel) == true) {
                    mCommentAudioModel?.isRead = true
                    mRedIv.visibility = View.GONE
                }
            }
        })
    }

    fun bind(position: Int, model: CommentAudioModel) {
        this.position = position
        this.mCommentAudioModel = model

        var duration = Math.round((model.duration.toFloat() / 1000.toFloat()).toDouble()).toInt()
        if (duration > 15) {
            duration = 15
        }
        val width = when (duration) {
            in 0..5 -> minSize
            in 6..10 -> minSize + (maxSize - minSize) / 5 * (duration - 5)
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

        if (model.userInfo != null
                && model.userInfo.ranking != null
                && model.userInfo.ranking.mainRanking >= UserLevelType.SKRER_LEVEL_SILVER) {
            val drawable = U.getDrawable(LevelConfigUtils.getSmallImageResoucesLevel(model.userInfo.ranking.mainRanking))
            drawable.setBounds(0, 0, U.getDisplayUtils().dip2px(22f), U.getDisplayUtils().dip2px(19f))
            val spannableStringBuilder = SpanUtils()
                    .appendImage(drawable, SpanUtils.ALIGN_CENTER)
                    .append(model.stringBuilder)
                    .create()
            mNameTv.text = spannableStringBuilder
        } else {
            mNameTv.text = model.stringBuilder
        }
        mAudioTv.text = duration.toString() + "s"
        mAvatarIv.bindData(model.userInfo)
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
