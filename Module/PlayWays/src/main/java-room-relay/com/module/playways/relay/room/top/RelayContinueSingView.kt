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
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.relay.room.RelayRoomData
import com.module.playways.relay.room.event.RelayLockChangeEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private val mCountTv: ExTextView

    val countDownTime = 8000L

    var roomData: RelayRoomData? = null

    var countDownJob: Job? = null

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

        mCountTv = findViewById<View>(R.id.count_tv) as ExTextView
    }

    fun startCountDown() {
        countDownJob?.cancel()
        countDownJob = launch {
            repeat(8) {
                delay(1000)
                mCountTv.text = "${7 - it}s"
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelayLockChangeEvent) {
        if (roomData?.unLockMe == true) {
            hideWithAnimation(false)
        }
    }

    fun delayShowContinueView(ts: Long) {
        hideWithAnimation(false)

        var t = roomData?.configModel?.durationTimeMs ?: 0

        var leftTs = t - ts
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
        countDownJob?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mUiHandler.removeCallbacksAndMessages(null)
        countDownJob?.cancel()
        EventBus.getDefault().unregister(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

}
