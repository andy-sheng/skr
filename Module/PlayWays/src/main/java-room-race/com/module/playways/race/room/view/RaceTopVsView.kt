package com.module.playways.race.room.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.common.anim.svga.SvgaParserAdapter
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.component.busilib.view.CircleCountDownView
import com.component.busilib.view.VoiceChartView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.event.RaceBlightEvent
import com.opensource.svgaplayer.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


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
    val leftSvgaIv: SVGAImageView
    val rightSvgaIv: SVGAImageView
    var leftVoiceChartView: VoiceChartView
    var rightVoiceChartView: VoiceChartView

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
        leftSvgaIv = this.findViewById(R.id.left_svga_iv)
        rightSvgaIv = this.findViewById(R.id.right_svga_iv)
        leftVoiceChartView = this.findViewById(R.id.left_voice_chart_view)
        rightVoiceChartView = this.findViewById(R.id.right_voice_chart_view)

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceBlightEvent) {
        updateAvatar()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
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
                if ((roomData?.realRoundInfo?.isSingerByUserId(MyUserInfoManager.uid.toInt()) == false)) {

                    resetLeftPlayCount(it[0].bLightCnt - getTextContentInt(leftTicketCountTv))
                    leftTicketCountTv.text = it[0].bLightCnt.toString()
                    resetRightPlayCount(it[1].bLightCnt - getTextContentInt(rightTicketCountTv))
                    rightTicketCountTv.text = it[1].bLightCnt.toString()
                } else {
                    if (roomData?.realRoundInfo?.subRoundSeq == 1) {

                        if (roomData?.realRoundInfo?.isSingerNowByUserId(MyUserInfoManager.uid.toInt()) == true) {
                            resetLeftPlayCount(it[0].bLightCnt - getTextContentInt(leftTicketCountTv))
                            leftTicketCountTv.text = it[0].bLightCnt.toString()
                            rightTicketCountTv.text = "**"
                        } else {
                            leftTicketCountTv.text = "**"
                            rightTicketCountTv.text = "**"
                        }
                    } else if (roomData?.realRoundInfo?.subRoundSeq == 2) {

                        if (roomData?.realRoundInfo?.isSingerNowByUserId(MyUserInfoManager.uid.toInt()) == true) {
                            leftTicketCountTv.text = "**"
                            resetRightPlayCount(it[1].bLightCnt - getTextContentInt(rightTicketCountTv))
                            rightTicketCountTv.text = it[1].bLightCnt.toString()
                        } else {
                            resetLeftPlayCount(it[0].bLightCnt - getTextContentInt(leftTicketCountTv))
                            leftTicketCountTv.text = it[0].bLightCnt.toString()
                            rightTicketCountTv.text = "**"
                        }
                    }
                }
            }

            if (roomData?.realRoundInfo?.subRoundSeq == 1) {
                leftVoiceChartView.start()
                leftVoiceChartView.visibility = View.VISIBLE
                rightVoiceChartView.stop()
                rightVoiceChartView.visibility = View.GONE
            } else {
                rightVoiceChartView.start()
                rightVoiceChartView.visibility = View.VISIBLE
                leftVoiceChartView.stop()
                leftVoiceChartView.visibility = View.GONE
            }
        }
    }

    private fun getTextContentInt(view: TextView): Int {
        if (!TextUtils.isEmpty(view.text.toString()) && !view.text.toString().equals("**")) {
            return view.text.toString().toInt()
        }

        return 0
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
                if (roomData?.getPlayerOrWaiterInfo(it[0].userID)?.userId == MyUserInfoManager.uid.toInt()) {
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
        leftVoiceChartView.stop()
        rightVoiceChartView.stop()

        EventBus.getDefault().unregister(this)
    }

    fun bindData() {
        leftCircleCountDownView.visibility = View.GONE
        rightCircleCountDownView.visibility = View.GONE

        updateAvatar()
        updateData()
    }

    private fun updateAvatar() {
        roomData?.realRoundInfo?.subRoundInfo?.let {
            var avatarUrl = if (roomData?.isFakeForMe(roomData?.realRoundInfo?.subRoundInfo?.getOrNull(0)?.userID
                            ?: 0) == true) roomData?.getPlayerOrWaiterInfoModel(it.getOrNull(0)?.userID)?.fakeUserInfo?.avatarUrl else roomData?.getPlayerOrWaiterInfo(it.getOrNull(0)?.userID)?.avatar
            AvatarUtils.loadAvatarByUrl(leftAvatarIv, AvatarUtils.newParamsBuilder(avatarUrl)
                    .setCornerRadius(U.getDisplayUtils().dip2px(21f).toFloat())
                    .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                    .setBorderColor(U.getColor(R.color.white))
                    .build())
        }

        roomData?.realRoundInfo?.subRoundInfo?.let {
            var avatarUrl = if (roomData?.isFakeForMe(roomData?.realRoundInfo?.subRoundInfo?.getOrNull(1)?.userID
                            ?: 0) == true) roomData?.getPlayerOrWaiterInfoModel(it.getOrNull(1)?.userID)?.fakeUserInfo?.avatarUrl else roomData?.getPlayerOrWaiterInfo(it.getOrNull(1)?.userID)?.avatar

            AvatarUtils.loadAvatarByUrl(rightAvatarIv, AvatarUtils.newParamsBuilder(avatarUrl)
                    .setCornerRadius(U.getDisplayUtils().dip2px(21f).toFloat())
                    .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                    .setBorderColor(U.getColor(R.color.white))
                    .build())
        }
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
