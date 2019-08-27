package com.module.playways.race.room.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.Guideline
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R


//选择歌曲页
class RaceMiddleResultView : ExConstraintLayout {
    val mTag = "RaceTopVsView"

    val resultTv: ExTextView
    val leftConstraintLayout: ConstraintLayout
    val leftTicketCountTv: ExTextView
    val leftTicketTv: ExTextView
    val leftAvatarIv: BaseImageView
    val leftHeadIv: ExImageView
    val guidelineCenter: Guideline
    val rightConstraintLayout: ConstraintLayout
    val rightTicketCountTv: ExTextView
    val rightTicketTv: ExTextView
    val rightAvatarIv: BaseImageView
    val rightHeadIv: ExImageView
    val guidelineCenter2: Guideline
    val raceTopVsIv: ImageView

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, com.module.playways.R.layout.race_middle_vs_view_layout, this)

        resultTv = this.findViewById(R.id.result_tv)
        leftConstraintLayout = this.findViewById(R.id.left_constraintLayout)
        leftTicketCountTv = this.findViewById(R.id.left_ticket_count_tv)
        leftTicketTv = this.findViewById(R.id.left_ticket_tv)
        leftAvatarIv = this.findViewById(R.id.left_avatar_iv)
        leftHeadIv = this.findViewById(R.id.left_head_iv)
        guidelineCenter = this.findViewById(R.id.guidelineCenter)
        rightConstraintLayout = this.findViewById(R.id.right_constraintLayout)
        rightTicketCountTv = this.findViewById(R.id.right_ticket_count_tv)
        rightTicketTv = this.findViewById(R.id.right_ticket_tv)
        rightAvatarIv = this.findViewById(R.id.right_avatar_iv)
        rightHeadIv = this.findViewById(R.id.right_head_iv)
        guidelineCenter2 = this.findViewById(R.id.guidelineCenter2)
        raceTopVsIv = this.findViewById(R.id.race_top_vs_iv)


        AvatarUtils.loadAvatarByUrl(leftAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setCornerRadius(U.getDisplayUtils().dip2px(32f).toFloat())
                .build())

        AvatarUtils.loadAvatarByUrl(rightAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setCornerRadius(U.getDisplayUtils().dip2px(32f).toFloat())
                .build())
    }

    fun bindData() {

    }

    fun startVs() {
        val animatorLeft = ObjectAnimator.ofFloat(leftConstraintLayout, "translationX", -(U.getDisplayUtils().phoneWidth.toFloat() / 2), 0f)

        val animatorRight = ObjectAnimator.ofFloat(rightConstraintLayout, "translationX", U.getDisplayUtils().phoneWidth.toFloat(), 0f)

        val animSet = AnimatorSet()
        animSet.play(animatorLeft).with(animatorRight)
        animSet.duration = 400

        animSet.start()
    }
}
