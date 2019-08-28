package com.module.playways.race.room.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.countdown.CircleCountDownView
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.module.playways.race.room.RaceRoomData


class RaceTopVsView : ExConstraintLayout {
    val mTag = "RaceTopVsView"

    val leftConstraintLayout: ConstraintLayout
    val leftTicketTv: ExTextView
    val leftTicketCountTv: ExTextView
    val leftAvatarIv: BaseImageView
    val leftCircleCountDownView: CircleCountDownView
    val rightConstraintLayout: ConstraintLayout
    val rightTicketTv: ExTextView
    val rightTicketCountTv: ExTextView
    val rightAvatarIv: BaseImageView
    val rightCircleCountDownView: CircleCountDownView

    val raceTopVsIv: ImageView
    var roomData: RaceRoomData? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, com.module.playways.R.layout.race_top_vs_view_layout, this)
        leftConstraintLayout = this.findViewById(com.module.playways.R.id.left_constraintLayout)
        leftTicketTv = this.findViewById(com.module.playways.R.id.left_ticket_tv)
        leftTicketCountTv = this.findViewById(com.module.playways.R.id.left_ticket_count_tv)
        leftAvatarIv = this.findViewById(com.module.playways.R.id.left_avatar_iv)
        leftCircleCountDownView = this.findViewById(com.module.playways.R.id.left_circle_count_down_view)
        rightConstraintLayout = this.findViewById(com.module.playways.R.id.right_constraintLayout)
        rightTicketTv = this.findViewById(com.module.playways.R.id.right_ticket_tv)
        rightTicketCountTv = this.findViewById(com.module.playways.R.id.right_ticket_count_tv)
        rightAvatarIv = this.findViewById(com.module.playways.R.id.right_avatar_iv)
        rightCircleCountDownView = this.findViewById(com.module.playways.R.id.right_circle_count_down_view)
        raceTopVsIv = this.findViewById(com.module.playways.R.id.race_top_vs_iv)
    }

    fun setRaceRoomData(roomData: RaceRoomData) {
        this.roomData = roomData
    }

    fun updateData() {
        roomData?.realRoundInfo?.scores?.let {
            leftTicketCountTv.text = it[0].bLightCnt.toString()
        }

        roomData?.realRoundInfo?.scores?.let {
            rightTicketCountTv.text = it[1].bLightCnt.toString()
        }
    }

    fun startSingBySelf(call: (() -> Unit)?) {
        roomData?.realRoundInfo?.subRoundInfo?.let {
            if (roomData?.getUserInfo(it[0].userID)?.userId == MyUserInfoManager.getInstance().uid.toInt()) {
                leftCircleCountDownView.go(0, 3 * 1000) {
                    call?.invoke()
                }
            } else {
                rightCircleCountDownView.go(0, 3 * 1000) {
                    call?.invoke()
                }
            }
        }
    }

    fun startSingByOther(call: (() -> Unit)?) {
        roomData?.realRoundInfo?.subRoundInfo?.let {
            if (roomData?.getUserInfo(it[0].userID)?.userId != MyUserInfoManager.getInstance().uid.toInt()) {
                leftCircleCountDownView.go(0, 3 * 1000) {
                    call?.invoke()
                }
            } else {
                rightCircleCountDownView.go(0, 3 * 1000) {
                    call?.invoke()
                }
            }
        }
    }

    private fun bindData() {
        roomData?.realRoundInfo?.subRoundInfo?.let {
            AvatarUtils.loadAvatarByUrl(leftAvatarIv, AvatarUtils.newParamsBuilder(roomData?.getUserInfo(it[0].userID)?.avatar)
                    .setCornerRadius(U.getDisplayUtils().dip2px(18f).toFloat())
                    .build())
        }

        roomData?.realRoundInfo?.subRoundInfo?.let {
            AvatarUtils.loadAvatarByUrl(rightAvatarIv, AvatarUtils.newParamsBuilder(roomData?.getUserInfo(it[1].userID)?.avatar)
                    .setCornerRadius(U.getDisplayUtils().dip2px(18f).toFloat())
                    .build())
        }

        updateData()
    }

    fun startVs() {
        visibility = View.VISIBLE
        bindData()
        val animatorLeft = ObjectAnimator.ofFloat(leftConstraintLayout, "translationX", -(U.getDisplayUtils().phoneWidth.toFloat() / 2), 0f)
        val animatorRight = ObjectAnimator.ofFloat(rightConstraintLayout, "translationX", U.getDisplayUtils().phoneWidth.toFloat(), 0f)
        val animSet = AnimatorSet()
        animSet.play(animatorLeft).with(animatorRight)
        animSet.duration = 400
        animSet.start()
    }
}
