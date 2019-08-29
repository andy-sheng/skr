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

// 无人想唱
class RaceNoSingerCardView : ConstraintLayout {

    val TAG = "TurnInfoCardView"

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val resultIv: ImageView

    internal var mEnterTranslateAnimation: TranslateAnimation? = null // 飞入的进场动画
    internal var mLeaveTranslateAnimation: TranslateAnimation? = null // 飞出的离场动画

    var mListener: AnimationListener? = null

    init {
        View.inflate(context, R.layout.race_result_info_card_layout, this);
        resultIv = this.findViewById(R.id.result_iv)
    }

    fun showAnimation(listener: AnimationListener) {
        // 入场，停一秒，离场, 肯定是一次播完的
        mListener = listener
        animationEnter()
        resultIv.postDelayed({
            animationLeave()
        }, 1200)
    }

    // 入场动画
    private fun animationEnter() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = TranslateAnimation((-U.getDisplayUtils().screenWidth).toFloat(), 0.0f, 0.0f, 0.0f)
            mEnterTranslateAnimation?.duration = 200
        }
        mEnterTranslateAnimation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
                visibility = View.VISIBLE
            }
        })
        this.startAnimation(mEnterTranslateAnimation)
    }

    // 离场动画
    private fun animationLeave() {
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
                    mListener?.onFinish()
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


    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mEnterTranslateAnimation?.setAnimationListener(null)
        mEnterTranslateAnimation?.cancel()

        mLeaveTranslateAnimation?.setAnimationListener(null)
        mLeaveTranslateAnimation?.cancel()

    }
}