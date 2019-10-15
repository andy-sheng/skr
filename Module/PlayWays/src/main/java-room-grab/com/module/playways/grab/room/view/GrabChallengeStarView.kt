package com.module.playways.grab.room.view


import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U

import com.common.view.ExViewStub
import com.module.playways.R

/**
 * 其他人主场景
 */
class GrabChallengeStarView(mViewStub: ViewStub?) : ExViewStub(mViewStub) {
    val TAG = "GrabChallengeStarView"

    var challengeBgIv: ImageView? = null
    var challengingTv: TextView? = null
    var challengStarCntTv: TextView? = null
    var challengeStarIv: ImageView? = null
    var clickListener: (() -> Unit)? = null

    val handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }

    override fun init(parentView: View) {
        challengeBgIv = parentView.findViewById(R.id.challenge_bg_iv)
        challengingTv = parentView.findViewById(R.id.challenging_tv)
        challengStarCntTv = parentView.findViewById(R.id.challenge_star_cnt_tv)
        challengeStarIv = parentView.findViewById(R.id.challenge_star_iv)
        parentView.setDebounceViewClickListener {
            clickListener?.invoke()
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_challenge_star_view_layout
    }

    fun bindData(cnt: Int, justShowInChallenge: Boolean, continueShow: Boolean) {
        tryInflate()
        if (justShowInChallenge) {
            challengingTv?.visibility = View.VISIBLE
            challengStarCntTv?.visibility = View.GONE
            challengeStarIv?.visibility = View.GONE
        } else {
            challengingTv?.visibility = View.GONE
            challengStarCntTv?.visibility = View.VISIBLE
            challengeStarIv?.visibility = View.VISIBLE
            challengStarCntTv?.text = "评价:${cnt}"

            val propertyValuesHolder1 = PropertyValuesHolder.ofFloat(View.SCALE_X, 2f, 1f)
            val propertyValuesHolder2 = PropertyValuesHolder.ofFloat(View.SCALE_Y, 2f, 1f)
            val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(challengeStarIv, propertyValuesHolder1, propertyValuesHolder2)
            objectAnimator.duration = 500
            objectAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(animation: Animator?) {
                    onAnimationEnd(null)
                }

                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    handler.postDelayed({
                        if (continueShow) {
                            bindData(0, justShowInChallenge = true, continueShow = false)
                        } else {
                            setVisibility(View.GONE)
                        }
                    }, 4000)
                }

            })
            objectAnimator.start()
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        handler.removeCallbacksAndMessages(null)
    }
}
