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
import com.common.anim.svga.SvgaParserAdapter
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
import com.opensource.svgaplayer.*
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
    val leftSvgaIv: SVGAImageView
    val rightSvgaIv: SVGAImageView

    val raceTopVsIv: ImageView
    var roomData: RaceRoomData? = null

    var leftPlayCount = 0
    var rightPlayCount = 0

    var leftPlaying = false
    var rightPlaying = false

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
        leftSvgaIv = this.findViewById(R.id.left_svga_iv)
        rightSvgaIv = this.findViewById(R.id.right_svga_iv)

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

    private fun tryPlayLeftAnima() {
        MyLog.d(TAG, "tryPlayLeftAnima")
        if (!leftPlaying) {
            leftPlaying = true
            leftPlayCount--
            MyLog.d(TAG, "tryPlayLeftAnima leftPlayCount is $leftPlayCount")
            leftSvgaIv.clearAnimation()
            leftSvgaIv.visibility = View.VISIBLE
            leftSvgaIv.loops = 1
            SvgaParserAdapter.parse("vote_star.svga", object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    MyLog.d(TAG, "tryPlayLeftAnima SvgaParserAdapter.parse")
                    val drawable = SVGADrawable(videoItem)
                    leftSvgaIv.setImageDrawable(drawable)
                    leftSvgaIv.startAnimation()
                }

                override fun onError() {

                }
            })

            leftSvgaIv.callback = object : SVGACallback {
                override fun onPause() {

                }

                override fun onFinished() {
                    MyLog.d(TAG, "tryPlayLeftAnima onFinished")
                    leftSvgaIv.visibility = View.GONE
                    leftPlaying = false
                    if (leftPlayCount > 0) {
                        tryPlayLeftAnima()
                    }
                }

                override fun onRepeat() {

                }

                override fun onStep(i: Int, v: Double) {

                }
            }
        }
    }

    private fun tryPlayRightAnima() {
        if (!rightPlaying) {
            rightPlaying = true
            rightPlayCount--
            rightSvgaIv.clearAnimation()
            rightSvgaIv.visibility = View.VISIBLE
            rightSvgaIv.loops = 1
            SvgaParserAdapter.parse("vote_star.svga", object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    val drawable = SVGADrawable(videoItem)
                    rightSvgaIv.setImageDrawable(drawable)
                    rightSvgaIv.startAnimation()
                }

                override fun onError() {

                }
            })

            rightSvgaIv.callback = object : SVGACallback {
                override fun onPause() {

                }

                override fun onFinished() {
                    rightSvgaIv.visibility = View.GONE
                    rightPlaying = false
                    if (rightPlayCount > 0) {
                        tryPlayRightAnima()
                    }
                }

                override fun onRepeat() {

                }

                override fun onStep(i: Int, v: Double) {

                }
            }
        }
    }

    fun updateData() {
        roomData?.realRoundInfo?.scores?.let {
            if (it.size == 2) {
                if (!(roomData?.realRoundInfo?.isSingerByUserId(MyUserInfoManager.getInstance().uid.toInt())
                                ?: false)) {

                    leftTicketGroup.visibility = View.VISIBLE
                    rightTicketGroup.visibility = View.VISIBLE
                    leftState.visibility = View.GONE
                    rightState.visibility = View.GONE

                    resetLeftPlayCount(it[0].bLightCnt - leftTicketCountTv.text.toString().toInt())
                    leftTicketCountTv.text = it[0].bLightCnt.toString()
                    resetRightPlayCount(it[1].bLightCnt - rightTicketCountTv.text.toString().toInt())
                    rightTicketCountTv.text = it[1].bLightCnt.toString()
                } else {
                    leftTicketGroup.visibility = View.GONE
                    rightTicketGroup.visibility = View.GONE
                    leftState.visibility = View.GONE
                    rightState.visibility = View.GONE
                    leftState.text = ""
                    rightState.text = ""

                    if (roomData?.realRoundInfo?.subRoundSeq == 1) {

                        if (roomData?.realRoundInfo?.isSingerNowByUserId(MyUserInfoManager.getInstance().uid.toInt())
                                        ?: true) {

                            resetLeftPlayCount(it[0].bLightCnt - leftTicketCountTv.text.toString().toInt())
                            leftTicketCountTv.text = it[0].bLightCnt.toString()
                            leftTicketGroup.visibility = View.VISIBLE
                            rightState.visibility = View.VISIBLE
                            rightState.text = "**"
                        } else {
                            rightState.text = "**"
                            rightState.visibility = View.VISIBLE
                            leftState.text = "**"
                            leftState.visibility = View.VISIBLE
                        }
                    } else if (roomData?.realRoundInfo?.subRoundSeq == 2) {

                        if (roomData?.realRoundInfo?.isSingerNowByUserId(MyUserInfoManager.getInstance().uid.toInt())
                                        ?: true) {

                            leftState.text = "**"
                            leftState.visibility = View.VISIBLE
                            rightTicketGroup.visibility = View.VISIBLE
                            resetRightPlayCount(it[1].bLightCnt - rightTicketCountTv.text.toString().toInt())
                            rightTicketCountTv.text = it[1].bLightCnt.toString()
                        } else {

                            leftTicketGroup.visibility = View.VISIBLE
                            resetLeftPlayCount(it[0].bLightCnt - leftTicketCountTv.text.toString().toInt())
                            leftTicketCountTv.text = it[0].bLightCnt.toString()
                            rightState.text = "**"
                            rightState.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun resetLeftPlayCount(addCount: Int) {
        if (addCount > 0) {
            leftPlayCount = leftPlayCount + addCount
            tryPlayLeftAnima()
        }
    }

    private fun resetRightPlayCount(addCount: Int) {
        if (addCount > 0) {
            rightPlayCount = rightPlayCount + addCount
            tryPlayRightAnima()
        }
    }

    fun startSingBySelf(call: (() -> Unit)?) {
        roomData?.realRoundInfo?.subRoundInfo?.let {
            if (it.size == 2) {
                if (roomData?.getPlayerOrWaiterInfo(it[0].userID)?.userId == MyUserInfoManager.getInstance().uid.toInt()) {
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        leftSvgaIv.stopAnimation()
        rightSvgaIv.stopAnimation()
        leftPlayCount = 0
        rightPlayCount = 0
    }

    fun bindData() {
        leftCircleCountDownView.visibility = View.GONE
        rightCircleCountDownView.visibility = View.GONE
        roomData?.realRoundInfo?.subRoundInfo?.let {
            AvatarUtils.loadAvatarByUrl(leftAvatarIv, AvatarUtils.newParamsBuilder(roomData?.getPlayerOrWaiterInfo(it[0].userID)?.avatar)
                    .setCornerRadius(U.getDisplayUtils().dip2px(18f).toFloat())
                    .build())
        }

        roomData?.realRoundInfo?.subRoundInfo?.let {
            AvatarUtils.loadAvatarByUrl(rightAvatarIv, AvatarUtils.newParamsBuilder(roomData?.getPlayerOrWaiterInfo(it[1].userID)?.avatar)
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
