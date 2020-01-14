package com.module.playways.party.room.seat

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Message
import android.support.constraint.Group
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.view.animation.CycleInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.AutoPollRecyclerView
import com.component.busilib.view.SpeakingTipsAnimationView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.party.room.model.PartyActorInfoModel
import com.module.playways.party.room.model.PartyEmojiInfoModel
import com.zq.live.proto.PartyRoom.EMicStatus
import com.zq.live.proto.PartyRoom.ESeatStatus
import java.lang.ref.WeakReference

// 正常位置
class SeatViewHolder(item: View, var listener: PartySeatAdapter.Listener?) : RecyclerView.ViewHolder(item) {

    private val avatarSdv: SimpleDraweeView = item.findViewById(R.id.avatar_sdv)
    private val hotTv: ExTextView = item.findViewById(R.id.hot_tv)
    private val nameTv: TextView = item.findViewById(R.id.name_tv)
    private val muteArea: Group = item.findViewById(R.id.mute_area)
    private val muteBg: ExImageView = item.findViewById(R.id.mute_bg)
    private val muteIv: ImageView = item.findViewById(R.id.mute_iv)
    private val speakerAnimationIv: SpeakingTipsAnimationView = item.findViewById(R.id.speaker_animation_iv)

    private val rollIv: ImageView = item.findViewById(R.id.roll_iv)
    private val emojiSdv: SimpleDraweeView = item.findViewById(R.id.emoji_sdv)

    var animation: ObjectAnimator? = null
    var animationRoll: AnimationDrawable? = null
    var mModel: PartyActorInfoModel? = null
    var mPos: Int = -1

    val MSG_TYPE_END_ROLLING = 0x01
    val MSG_TYPE_END_ANIMATION = 0x02

    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
                MSG_TYPE_END_ROLLING -> {
                    animationRoll?.stop()
                    rollIv.clearAnimation()
                    emojiSdv.visibility = View.VISIBLE
                    rollIv.visibility = View.GONE
                }
                MSG_TYPE_END_ANIMATION -> {
                    rollIv.visibility = View.GONE
                    emojiSdv.visibility = View.GONE
                }

            }
        }
    }

    init {
        avatarSdv.setDebounceViewClickListener {
            listener?.onClickItem(mPos, mModel)
        }
    }

    fun bindData(position: Int, model: PartyActorInfoModel?) {
        this.mModel = model
        this.mPos = position

        AvatarUtils.loadAvatarByUrl(avatarSdv,
                AvatarUtils.newParamsBuilder(model?.player?.userInfo?.avatar)
                        .setCircle(true)
                        .setBorderWidth(1.dp().toFloat())
                        .setBorderColor(Color.WHITE)
                        .build())
        nameTv.text = "${model?.player?.userInfo?.nicknameRemark}"
        refreshHot()
        refreshMute()
        stopSpeakAnimation()
    }

    fun refreshMute() {
        if (mModel?.seat?.micStatus == EMicStatus.MS_CLOSE.value) {
            muteArea.visibility = View.VISIBLE
        } else {
            stopSpeakAnimation()
            muteArea.visibility = View.GONE
        }
    }

    fun refreshHot() {
        hotTv.text = "${mModel?.player?.popularity ?: 0}"
    }

    fun playSpeakAnimation() {
        stopSpeakAnimation()
        speakerAnimationIv.show(1000)
    }

    fun stopSpeakAnimation() {
        speakerAnimationIv.hide()
    }

    fun playEmojiAnimation(model: PartyEmojiInfoModel) {
        stop()
        FrescoWorker.loadImage(emojiSdv, ImageFactory.newPathImage(model.bigEmojiURL)
                .build())
        if (model.id == 1000) {
            // 骰子
            emojiSdv.visibility = View.GONE
            rollIv.visibility = View.VISIBLE

            animationRoll = U.getDrawable(R.drawable.party_rolling_anmation) as AnimationDrawable
            rollIv.setImageDrawable(animationRoll)
            animationRoll?.start()

            handler.sendEmptyMessageDelayed(MSG_TYPE_END_ROLLING,2000)
            handler.sendEmptyMessageDelayed(MSG_TYPE_END_ANIMATION,4000)
        } else {
            // 普通表情
            emojiSdv.visibility = View.VISIBLE
            rollIv.visibility = View.GONE

            animation = ObjectAnimator.ofFloat(emojiSdv, View.TRANSLATION_Y, -10.dp().toFloat(), 0f)
            animation?.interpolator = CycleInterpolator(3f)
            animation?.duration = 2000
            animation?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    emojiSdv.visibility = View.GONE
                }
            })
            animation?.start()
        }
    }

    private fun stop() {
        animation?.removeAllListeners()
        animation?.cancel()
        handler?.removeCallbacksAndMessages(null)
        animationRoll?.stop()
    }

}


// 空座位
class EmptySeatViewHolder(item: View, var listener: PartySeatAdapter.Listener?) : RecyclerView.ViewHolder(item) {

    private val emptyBg: ExImageView = item.findViewById(R.id.empty_bg)
    private val emptyTv: TextView = item.findViewById(R.id.empty_tv)

    var mPos: Int = -1
    var mModel: PartyActorInfoModel? = null

    init {
        emptyBg.setDebounceViewClickListener {
            listener?.onClickItem(mPos, mModel)
        }
    }

    fun bindData(position: Int, model: PartyActorInfoModel?) {
        this.mPos = position
        this.mModel = model

        if (model?.seat?.seatStatus == ESeatStatus.SS_CLOSE.value) {
            // 关闭席位的UI
            emptyTv.text = "已关闭"
            emptyTv.setTextColor(U.getColor(R.color.white_trans_50))
            emptyTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14.dp().toFloat())
        } else {
            emptyTv.text = "${position + 1}"
            emptyTv.setTextColor(U.getColor(R.color.white_trans_80))
            emptyTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24.dp().toFloat())
        }
    }
}