package com.module.playways.relay.room.top

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import com.common.log.MyLog
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExRelativeLayout
import com.module.playways.R
import com.module.playways.grab.room.view.RoundRectangleView
import com.module.playways.relay.room.RelayRoomData
import com.module.playways.relay.room.event.RelayLockChangeEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 不唱了界面
 */
class RelayContinueSingView : ExRelativeLayout {
    val mTag = "RelayContinueSingView"

    val MSG_ANIMATION_SHOW = 1
    val MSG_ANIMATION_HIDE = 2

    private val mIvContinue: ExImageView
    private val mRoundRectangleView: RoundRectangleView

    val countDownTime = 8000L

    var roomData: RelayRoomData? = null

    internal var mContinueListener: (() -> Unit)? = null

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_ANIMATION_SHOW) {
                val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
                animation.duration = 200
                animation.repeatMode = Animation.REVERSE
                animation.interpolator = OvershootInterpolator()
                animation.fillAfter = true
                startAnimation(animation)
                visibility = View.VISIBLE
                startCountDown()
            } else if (msg.what == MSG_ANIMATION_HIDE) {
                hideWithAnimation(false)
            }
        }
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.relay_continue_sing_view_layout, this)
        mIvContinue = findViewById<View>(R.id.continue_iv) as ExImageView
        mIvContinue.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mContinueListener?.invoke()
            }
        })

        mRoundRectangleView = findViewById<View>(R.id.rrl_progress) as RoundRectangleView
    }

    fun startCountDown() {
        mUiHandler.postDelayed({
            mRoundRectangleView?.startCountDown(countDownTime)
        }, 500)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelayLockChangeEvent) {
        if (roomData?.unLockMe == true) {
            hideWithAnimation(false)
        }
    }

    fun delayShowContinueView(ts: Long) {
        hideWithAnimation(false)

        var t = roomData?.realRoundInfo?.singEndMs!! - roomData?.realRoundInfo?.singBeginMs!!

        var leftTs = t - (roomData?.getSingCurPosition() ?: 0) - ts
        if (leftTs < 0) {
            leftTs = 0
        }

        mUiHandler.removeMessages(MSG_ANIMATION_SHOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_ANIMATION_SHOW, leftTs)
        mUiHandler.sendEmptyMessageDelayed(MSG_ANIMATION_HIDE, leftTs + countDownTime)
    }

    fun hideWithAnimation(needAnim: Boolean) {
        MyLog.d(mTag, "hideWithAnimation needAnim=$needAnim")
        mUiHandler.removeMessages(MSG_ANIMATION_SHOW)
        clearAnimation()
        visibility = View.GONE
        mRoundRectangleView.stopCountDown()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mUiHandler.removeCallbacksAndMessages(null)
        mRoundRectangleView.stopCountDown()
        EventBus.getDefault().unregister(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

}
