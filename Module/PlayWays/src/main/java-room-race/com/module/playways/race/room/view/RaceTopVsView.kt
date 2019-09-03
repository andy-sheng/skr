package com.module.playways.race.room.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.Group
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.countdown.CircleCountDownView
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class RaceTopVsView : ExConstraintLayout {
    val TAG = "RaceTopVsView"

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
    val leftState: ExTextView
    val leftTicketGroup: Group
    val rightState: ExTextView
    val rightTicketGroup: Group

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
        leftState = this.findViewById(R.id.left_state)
        leftTicketGroup = this.findViewById(R.id.left_ticket_group)
        rightState = this.findViewById(R.id.right_state)
        rightTicketGroup = this.findViewById(R.id.right_ticket_group)

        leftAvatarIv.setDebounceViewClickListener {
            roomData?.realRoundInfo?.subRoundInfo?.let {
                if (it.size == 2) {
                    EventBus.getDefault().post(ShowPersonCardEvent(it[0].userID))
                }
            }
        }

        rightAvatarIv.setDebounceViewClickListener {
            roomData?.realRoundInfo?.subRoundInfo?.let {
                if (it.size == 2) {
                    EventBus.getDefault().post(ShowPersonCardEvent(it[1].userID))
                }
            }
        }
    }

    fun setRaceRoomData(roomData: RaceRoomData) {
        this.roomData = roomData
    }

    fun updateData() {
        roomData?.realRoundInfo?.scores?.let {
            if (!(roomData?.realRoundInfo?.isSingerByUserId(MyUserInfoManager.getInstance().uid.toInt())
                            ?: false)) {
                MyLog.d(TAG, "updateData 1")
                leftTicketGroup.visibility = View.VISIBLE
                rightTicketGroup.visibility = View.VISIBLE
                leftState.visibility = View.GONE
                rightState.visibility = View.GONE

                leftTicketCountTv.text = it[0].bLightCnt.toString()
                rightTicketCountTv.text = it[1].bLightCnt.toString()
            } else {
                leftTicketGroup.visibility = View.GONE
                rightTicketGroup.visibility = View.GONE
                leftState.visibility = View.VISIBLE
                rightState.visibility = View.VISIBLE
                leftState.text = ""
                rightState.text = ""

                if (roomData?.realRoundInfo?.subRoundSeq == 1) {
                    MyLog.d(TAG, "updateData 2")
                    if (roomData?.realRoundInfo?.isSingerNowByUserId(MyUserInfoManager.getInstance().uid.toInt())
                                    ?: true) {
                        MyLog.d(TAG, "updateData 3")
                        leftTicketCountTv.text = it[0].bLightCnt.toString()
                        leftTicketGroup.visibility = View.VISIBLE
                    } else {
                        MyLog.d(TAG, "updateData 4")
                        leftState.text = "投票中"
                    }
                } else if (roomData?.realRoundInfo?.subRoundSeq == 2) {
                    MyLog.d(TAG, "updateData 5")
                    if (roomData?.realRoundInfo?.isSingerNowByUserId(MyUserInfoManager.getInstance().uid.toInt())
                                    ?: true) {
                        MyLog.d(TAG, "updateData 6")
                        leftState.text = "锁票"
                        rightTicketGroup.visibility = View.VISIBLE
                        rightTicketCountTv.text = it[1].bLightCnt.toString()
                    } else {
                        MyLog.d(TAG, "updateData 7")
                        leftTicketGroup.visibility = View.VISIBLE
                        leftTicketCountTv.text = it[0].bLightCnt.toString()
                        rightState.text = "投票中"
                    }
                }
            }
        }
    }

    fun startSingBySelf(call: (() -> Unit)?) {
        roomData?.realRoundInfo?.subRoundInfo?.let {
            if (roomData?.getUserInfo(it[0].userID)?.userId == MyUserInfoManager.getInstance().uid.toInt()) {
                leftCircleCountDownView.visibility = View.VISIBLE
                leftCircleCountDownView.go(0, 1 * 1000) {
                    leftCircleCountDownView.visibility = View.GONE
                    call?.invoke()
                    updateData()
                }
            } else {
                rightCircleCountDownView.visibility = View.VISIBLE
                rightCircleCountDownView.go(0, 1 * 1000) {
                    rightCircleCountDownView.visibility = View.GONE
                    call?.invoke()
                    updateData()
                }
            }
        }
    }

    fun startSingByOther(round: Int, call: (() -> Unit)?) {
        roomData?.realRoundInfo?.subRoundInfo?.let {
            if (round == 1) {
                leftCircleCountDownView.visibility = View.VISIBLE
                leftCircleCountDownView.go(0, 1 * 1000) {
                    leftCircleCountDownView.visibility = View.GONE
                    call?.invoke()
                    updateData()
                }
            } else {
                rightCircleCountDownView.visibility = View.VISIBLE
                rightCircleCountDownView.go(0, 1 * 1000) {
                    rightCircleCountDownView.visibility = View.GONE
                    call?.invoke()
                    updateData()
                }
            }
        }
    }

    fun bindData() {
        leftCircleCountDownView.visibility = View.GONE
        rightCircleCountDownView.visibility = View.GONE
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
        raceTopVsIv.visibility = View.GONE
        visibility = View.VISIBLE
        bindData()
        val animatorLeft = ObjectAnimator.ofFloat(leftConstraintLayout, "translationX", -(U.getDisplayUtils().phoneWidth.toFloat() / 2), 0f)
        val animatorRight = ObjectAnimator.ofFloat(rightConstraintLayout, "translationX", U.getDisplayUtils().phoneWidth.toFloat(), 0f)
        val animSet = AnimatorSet()
        animSet.play(animatorLeft).with(animatorRight)
        animSet.duration = 400
        animSet.start()

        launch {
            delay(350)
            updateData()
            val animatorSet = ObjectAnimator.ofPropertyValuesHolder(raceTopVsIv,
                    PropertyValuesHolder.ofFloat("scaleX", 2.0f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 2.0f, 1f))
            raceTopVsIv.visibility = View.VISIBLE
            animatorSet.duration = 500
            animatorSet.interpolator = OvershootInterpolator()
            animatorSet.start()
        }
    }
}
