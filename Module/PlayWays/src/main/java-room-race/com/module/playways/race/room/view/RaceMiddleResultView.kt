package com.module.playways.race.room.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.Guideline
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import com.zq.live.proto.RaceRoom.ERaceWinType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//选择歌曲页
class RaceMiddleResultView : ExConstraintLayout {
    val TAG = "RaceTopVsView"

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
    var roomData: RaceRoomData? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.race_middle_vs_view_layout, this)

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
    }

    fun setRaceRoomData(roomData: RaceRoomData) {
        this.roomData = roomData
    }

    fun showResult(lastRound:RaceRoundInfoModel,animationOverListener:()->Unit) {
        lastRound?.let {
            if (it.status == ERaceRoundStatus.ERRS_END.value) {
                resultTv.visibility = View.GONE
                leftTicketCountTv.text = it.scores[0].bLightCnt.toString()
                if (it.scores[0].winType == ERaceWinType.RWT_WIN.value) {
                    leftHeadIv.visibility = View.VISIBLE
                } else {
                    leftHeadIv.visibility = View.GONE
                }

                if (it.scores[0].winType == ERaceWinType.RWT_WIN.value || it.scores[0].winType == ERaceWinType.RWT_LOSE.value) {
                    resultTv.background = U.getDrawable(R.drawable.race_result_win)
                } else {
                    resultTv.background = U.getDrawable(R.drawable.race_result_draw)
                }

                if (it.scores[0].isEscape) {
                    leftTicketTv.visibility = View.GONE
                    leftTicketCountTv.text = "逃跑"
                } else {
                    leftTicketTv.visibility = View.VISIBLE
                }

                AvatarUtils.loadAvatarByUrl(leftAvatarIv, AvatarUtils.newParamsBuilder(roomData?.getUserInfo(it.subRoundInfo[0].userID)?.avatar)
                        .setCornerRadius(U.getDisplayUtils().dip2px(32f).toFloat())
                        .build())

                rightTicketCountTv.text = it.scores[1].bLightCnt.toString()
                if (it.scores[1].winType == ERaceWinType.RWT_WIN.value) {
                    rightHeadIv.visibility = View.VISIBLE
                } else {
                    rightHeadIv.visibility = View.GONE
                }

                if (it.scores[1].isEscape) {
                    rightTicketTv.visibility = View.GONE
                    rightTicketCountTv.text = "逃跑"
                } else {
                    rightTicketTv.visibility = View.VISIBLE
                }

                AvatarUtils.loadAvatarByUrl(rightAvatarIv, AvatarUtils.newParamsBuilder(roomData?.getUserInfo(it.subRoundInfo[1].userID)?.avatar)
                        .setCornerRadius(U.getDisplayUtils().dip2px(32f).toFloat())
                        .build())

            } else {
                MyLog.w(TAG, "showResult, 不是结束状态， value is ${ERaceRoundStatus.ERRS_END.value}")
            }
        }
        startVs()
        launch {
            delay(4000)
            // 让显示4秒钟
            animationOverListener.invoke()
        }
    }

    fun startVs() {
        raceTopVsIv.visibility = View.GONE
        val animatorLeft = ObjectAnimator.ofFloat(leftConstraintLayout, "translationX", -(U.getDisplayUtils().phoneWidth.toFloat() / 2), 0f)

        val animatorRight = ObjectAnimator.ofFloat(rightConstraintLayout, "translationX", U.getDisplayUtils().phoneWidth.toFloat(), 0f)

        val animSet = AnimatorSet()
        animSet.play(animatorLeft).with(animatorRight)
        animSet.duration = 400

        animSet.start()

        launch {
            delay(350)
            resultTv.visibility = View.VISIBLE
            val animatorSet = AnimatorSet()
            val scaleX = ObjectAnimator.ofFloat(raceTopVsIv, "scaleX", 2.0f, 1f)
            val scaleY = ObjectAnimator.ofFloat(raceTopVsIv, "scaleY", 2.0f, 1f)
            raceTopVsIv.visibility = View.VISIBLE
            animatorSet.setDuration(500)
            animatorSet.setInterpolator(OvershootInterpolator())
            animatorSet.play(scaleX).with(scaleY);//两个动画同时开始
            animatorSet.start()
        }
    }
}
