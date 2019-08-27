package com.module.playways.race.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import com.common.utils.U
import com.module.playways.R
import com.module.playways.listener.AnimationListener

/**
 * 等待中的卡片，只有一个离场的动画，无入场动画
 */
class RaceWaitingCardView : ConstraintLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val waitIv: ImageView

    internal var mEnterTranslateAnimation: TranslateAnimation? = null // 飞入的进场动画
    internal var mLeaveTranslateAnimation: TranslateAnimation? = null // 飞出的离场动画

    init {
        View.inflate(context, R.layout.race_wait_card_layout, this)
        waitIv = this.findViewById(R.id.wait_iv)
    }

    // 入场动画
    fun animationEnter() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = TranslateAnimation((-U.getDisplayUtils().screenWidth).toFloat(), 0.0f, 0.0f, 0.0f)
            mEnterTranslateAnimation?.duration = 200
        }
        mEnterTranslateAnimation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
                visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        })
        this.startAnimation(mEnterTranslateAnimation)
    }

    // 离场动画
    fun animationLeave(listener: AnimationListener?) {
        if (this != null && this.visibility == View.VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = TranslateAnimation(0.0f, U.getDisplayUtils().screenWidth.toFloat(), 0.0f, 0.0f)
                mLeaveTranslateAnimation?.duration = 200
            }
            mLeaveTranslateAnimation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    clearAnimation()
                    visibility = View.GONE
                    listener?.onFinish()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            this.startAnimation(mLeaveTranslateAnimation)
        } else {
            clearAnimation()
            visibility = View.GONE
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

    }
}