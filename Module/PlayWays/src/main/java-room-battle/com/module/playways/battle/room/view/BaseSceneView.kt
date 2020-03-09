package com.module.playways.battle.room.view

import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import com.common.view.ExViewStub

abstract class BaseSceneView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val MSG_ANIMATION_HIDE = 0
    val MSG_ANIMATION_SHOW = 1
    val MSG_GONE = 3

    private var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_ANIMATION_SHOW) {
                val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
                animation.duration = 500
                animation.repeatMode = Animation.REVERSE
                animation.interpolator = OvershootInterpolator()
                animation.fillAfter = true
                mParentView?.startAnimation(animation)
                setVisibility(View.VISIBLE)
            } else if (msg.what == MSG_ANIMATION_HIDE) {
                val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
                animation.duration = 500
                animation.repeatMode = Animation.REVERSE
                animation.interpolator = OvershootInterpolator()
                animation.fillAfter = true
                mParentView?.startAnimation(animation)
            } else if (msg.what == MSG_GONE) {
                setVisibility(View.GONE)
            }
        }
    }

    fun enterAnimation() {
        tryInflate()
        mUiHandler.removeCallbacksAndMessages(null)
        mUiHandler.sendEmptyMessage(MSG_ANIMATION_SHOW)
    }

    fun leaveAnimation() {
        mUiHandler.removeCallbacksAndMessages(null)
        mUiHandler.sendEmptyMessage(MSG_ANIMATION_HIDE)
        mUiHandler.sendEmptyMessageDelayed(MSG_GONE, 200)
    }
}
