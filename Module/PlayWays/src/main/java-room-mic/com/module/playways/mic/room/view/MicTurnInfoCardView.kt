package com.module.playways.mic.room.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.common.utils.U
import com.module.playways.R
import com.module.playways.listener.AnimationListener


class MicTurnInfoCardView : ConstraintLayout {

    val TAG = "MicTurnInfoCardView"

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val resultIv: ImageView

    internal var mEnterTranslateAnimation: ObjectAnimator? = null // 飞入的进场动画
    internal var mLeaveTranslateAnimation: ObjectAnimator? = null // 飞出的离场动画

    var mListener: AnimationListener? = null

    init {
        View.inflate(context, R.layout.mic_turn_info_card_layout, this);
        resultIv = this.findViewById(R.id.result_iv)
    }

    fun showAnimation(listener: AnimationListener) {
        visibility = View.VISIBLE
        // 入场，停一秒，离场, 肯定是一次播完的
        mListener = listener
        animationEnter()
        resultIv.postDelayed({
            animationLeave()
        }, 1200)
    }

    // 入场动画
    private fun animationEnter() {
        visibility = View.VISIBLE
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = ObjectAnimator.ofFloat(this, "translationX", -U.getDisplayUtils().screenWidth.toFloat(), 0.0f)
            mEnterTranslateAnimation?.duration = 200
        }

        mEnterTranslateAnimation?.start()
    }

    // 离场动画
    private fun animationLeave() {
        if (this != null && this.visibility == View.VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = ObjectAnimator.ofFloat(this, "translationX", 0.0f, U.getDisplayUtils().screenWidth.toFloat())
                mLeaveTranslateAnimation?.duration = 200
            }
            mLeaveTranslateAnimation?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    clearAnimation()
                    visibility = View.GONE
                    mListener?.onFinish()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })

            mLeaveTranslateAnimation?.start()
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
        mEnterTranslateAnimation?.removeAllListeners()
        mEnterTranslateAnimation?.cancel()

        mLeaveTranslateAnimation?.removeAllListeners()
        mLeaveTranslateAnimation?.cancel()

    }
}
