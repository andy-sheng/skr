package com.module.playways.party.room.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import com.common.utils.U
import com.module.playways.party.room.PartyRoomActivity
import com.module.playways.relay.room.RelayRoomActivity
import java.util.*


/**
 * 来控制执行视频模式下面板的一些复杂动画
 */
class PartyWidgetAnimationController(internal var mF: PartyRoomActivity) {

    var openType = OPEN_TYPE_FOR_NORMAL
    var isOpen = true
        internal set
    internal var mMainAnimatorSet: AnimatorSet? = null

    private val translateByOpenType: Int
        get() {
            if (openType == OPEN_TYPE_FOR_NORMAL) {
                return U.getDisplayUtils().dip2px(40f)
            } else if (openType == OPEN_TYPE_FOR_LYRIC) {
                return U.getDisplayUtils().dip2px(120f)
            }
            return 0
        }

    /**
     * 使得主区域下移到 view 的下方
     */
    fun openBelowLyricView() {
        openType = OPEN_TYPE_FOR_LYRIC
        open()
    }

    fun openBelowOpView() {
        openType = OPEN_TYPE_FOR_NORMAL
        open()
    }

    fun open() {
        if (mMainAnimatorSet != null && mMainAnimatorSet!!.isRunning) {
            mMainAnimatorSet!!.cancel()
        }
        // 需要改变偏移的对象
        val viewList = ArrayList<View?>()
        fillView(viewList)

        val animators = ArrayList<Animator>()
        for (view in viewList) {
            if (view != null) {
                var objectAnimator = ObjectAnimator.ofFloat<View>(view, View.TRANSLATION_Y, view.translationY, translateByOpenType.toFloat())
                animators.add(objectAnimator)
            }
        }
        mMainAnimatorSet = AnimatorSet()
        mMainAnimatorSet!!.playTogether(animators)
        mMainAnimatorSet!!.duration = 300
        mMainAnimatorSet!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mF.mTopContentView.setArrowIcon(true)
                if (openType == OPEN_TYPE_FOR_NORMAL) {
                    mF.mTopOpView.visibility = View.VISIBLE
                } else if (openType == OPEN_TYPE_FOR_LYRIC) {
                    mF.mTopOpView.visibility = View.GONE
                }
                isOpen = true
            }
        })
        mMainAnimatorSet!!.start()
    }

    private fun fillView(viewList: MutableList<View?>) {
        viewList.add(mF.mTopContentView)
        viewList.add(mF.mPartyGameMainView?.realView)
        viewList.add(mF.mSeatView)
    }

    /**
     * 使得主区域下移到 view 的下方
     */
    fun close() {
        if (mMainAnimatorSet != null && mMainAnimatorSet!!.isRunning) {
            mMainAnimatorSet!!.cancel()
        }
        // 需要改变偏移的对象
        val viewList = ArrayList<View?>()
        fillView(viewList)
        val animators = ArrayList<Animator>()
        for (view in viewList) {
            if (view != null) {
                val objectAnimator = ObjectAnimator.ofFloat<View>(view, View.TRANSLATION_Y, view.translationY, 0f)
                animators.add(objectAnimator)
            }
        }
        mMainAnimatorSet = AnimatorSet()
        mMainAnimatorSet!!.playTogether(animators)
        mMainAnimatorSet!!.duration = 300
        mMainAnimatorSet!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                mF.mTopOpView.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mF.mTopContentView.setArrowIcon(false)
                isOpen = false
            }
        })
        mMainAnimatorSet!!.start()
    }

    fun destroy() {
        mMainAnimatorSet?.cancel()
    }

    companion object {

        val OPEN_TYPE_FOR_NORMAL = 1

        val OPEN_TYPE_FOR_LYRIC = 2
    }

}
