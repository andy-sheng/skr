package com.module.playways.grab.room.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout

import com.common.log.MyLog
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.playways.R

/**
 * 不唱了界面
 */
class GrabGiveupView : RelativeLayout {
    val mTag = "GrabPassView"

    val MSG_ANIMATION_SHOW = 1

    private val mIvPass: ExImageView
    private val mOwnerStopIv: ExImageView

    internal var mListener: Listener? = null

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
            }

        }
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setListener(listener: Listener) {
        mListener = listener
    }

    init {
        View.inflate(context, R.layout.grab_pass_view_layout, this)
        mIvPass = findViewById<View>(R.id.give_up_iv) as ExImageView
        mIvPass.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mListener != null) {
                    mListener!!.giveUp(false)
                }
            }
        })
        mOwnerStopIv = findViewById(R.id.owner_stop_iv)
        mOwnerStopIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mListener != null) {
                    mListener!!.giveUp(true)
                }
            }
        })
    }

    fun delayShowGiveUpView(ownerControl: Boolean) {
        if (ownerControl) {
            mOwnerStopIv.visibility = View.VISIBLE
            mIvPass.visibility = View.GONE
        } else {
            mOwnerStopIv.visibility = View.GONE
            mIvPass.visibility = View.VISIBLE
        }
        hideWithAnimation(false)
        mUiHandler.removeMessages(MSG_ANIMATION_SHOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_ANIMATION_SHOW, 5000)
    }

    fun giveUpSuccess() {
        mUiHandler.removeMessages(MSG_ANIMATION_SHOW)
        hideWithAnimation(true)
    }

    fun hideWithAnimation(needAnim: Boolean) {
        MyLog.d(mTag, "hideWithAnimation needAnim=$needAnim")
        mUiHandler.removeMessages(MSG_ANIMATION_SHOW)
        clearAnimation()
        visibility = View.GONE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mUiHandler.removeCallbacksAndMessages(null)
    }

    interface Listener {
        fun giveUp(ownerControl: Boolean)
    }
}
