package com.common.view.countdown

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar

class RecordProgressBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ProgressBar(context, attrs, defStyle) {

    internal var mRecordAnimator: ValueAnimator? = null

    fun go(start: Int, leave: Int) = go(start, leave, null)

    fun go(start: Int, leave: Int, completeListener: (() -> Unit)?) {
        cancelAnim()
        max = start + leave

        mRecordAnimator = ValueAnimator.ofInt(start, max)
        mRecordAnimator!!.duration = (if (leave > 0) leave else 0).toLong()
        mRecordAnimator!!.interpolator = LinearInterpolator()
        mRecordAnimator!!.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            progress = value
        }

        mRecordAnimator!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                completeListener?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

        })
        mRecordAnimator!!.start()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            cancelAnim()
        }
    }

    fun cancelAnim() {
        if (mRecordAnimator != null) {
            mRecordAnimator!!.removeAllUpdateListeners()
            mRecordAnimator!!.removeAllListeners()
            mRecordAnimator!!.cancel()
            mRecordAnimator = null
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAnim()
    }
}
