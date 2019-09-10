package com.module.playways.grab.room.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

import com.common.utils.U

import java.util.ArrayList


/**
 * 来控制执行视频模式下面板的一些复杂动画
 */
class GrabWidgetAnimationController(internal var mF: GrabRoomFragment) {

    var openType = OPEN_TYPE_FOR_NORMAL
    var isOpen = true
        internal set
    internal var mMainAnimatorSet: AnimatorSet? = null

    val translateByOpenType: Int
        get() {
            if (openType == OPEN_TYPE_FOR_NORMAL) {
                return U.getDisplayUtils().dip2px(32f)
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
                var objectAnimator: ObjectAnimator? = null
                if (view === mF.mGrabVideoDisplayView.realView) {
                    // 要多下移一个顶部状态栏的高度，才能和 ContentView对齐
                    objectAnimator = ObjectAnimator.ofFloat<View>(view, View.TRANSLATION_Y, view.translationY, (translateByOpenType + mF.mGrabVideoDisplayView.getExtraTranslateYWhenOpen(openType)).toFloat())
                } else {
                    objectAnimator = ObjectAnimator.ofFloat<View>(view, View.TRANSLATION_Y, view.translationY, translateByOpenType.toFloat())
                }
                animators.add(objectAnimator)
            }
        }
        val animators2 = mF.mGrabVideoDisplayView.getInnerAnimator(true, mF.mGrabTopContentView.visibility == View.VISIBLE)
        if (animators2 != null) {
            animators.addAll(animators2)
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
                mF.mGrabTopContentView.setArrowIcon(true)
                if (openType == OPEN_TYPE_FOR_NORMAL) {
                    mF.mGrabTopOpView.visibility = View.VISIBLE
                    mF.mGrabVideoSelfSingCardView!!.setVisibility(View.GONE)
                } else if (openType == OPEN_TYPE_FOR_LYRIC) {
                    mF.mGrabTopOpView.visibility = View.GONE
                    mF.mGrabVideoSelfSingCardView!!.setVisibility(View.VISIBLE)
                }
                isOpen = true
            }
        })
        mMainAnimatorSet!!.start()
        mF.mGameTipsManager.setBaseTranslateY(mF.TAG_INVITE_TIP_VIEW, translateByOpenType)
    }

    internal fun fillView(viewList: MutableList<View?>) {
        viewList.add(mF.mGrabTopContentView)
        viewList.add(mF.mPracticeFlagIv)
        viewList.add(mF.mGameTipsManager.getViewByKey(mF.TAG_SELF_SING_TIP_VIEW))
        viewList.add(mF.mGrabOpBtn)
        viewList.add(mF.mGrabGiveupView)
        viewList.add(mF.mTurnInfoCardView)
        viewList.add(mF.mSongInfoCardView)
        viewList.addAll(mF.mRoundOverCardView.realViews)
        viewList.add(mF.mGiftContinueViewGroup)
        mF.playbookWaitStatusIv?.let {
            viewList.add(it)
        }
        if (mF.mRoomData!!.isVideoRoom) {
            mF.mGrabVideoDisplayView.realView?.let {
                viewList.add(it)
            }
        } else {
            viewList.addAll(mF.mOthersSingCardView.realViews)
        }
        viewList.addAll(mF.mSelfSingCardView.realViews)
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
        val animators2 = mF.mGrabVideoDisplayView.getInnerAnimator(false, mF.mGrabTopContentView.visibility == View.VISIBLE)
        if (animators2 != null) {
            animators.addAll(animators2)
        }
        mMainAnimatorSet = AnimatorSet()
        mMainAnimatorSet!!.playTogether(animators)
        mMainAnimatorSet!!.duration = 300
        mMainAnimatorSet!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                mF.mGrabTopOpView.visibility = View.GONE
                mF.mGrabVideoSelfSingCardView!!.setVisibility(View.GONE)
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mF.mGrabTopContentView.setArrowIcon(false)
                isOpen = false
            }
        })
        mMainAnimatorSet!!.start()
        mF.mGameTipsManager.setBaseTranslateY(mF.TAG_INVITE_TIP_VIEW, 0)

    }

    fun destroy() {
        if (mMainAnimatorSet != null) {
            mMainAnimatorSet!!.cancel()
        }
    }

    companion object {

        val OPEN_TYPE_FOR_NORMAL = 1

        val OPEN_TYPE_FOR_LYRIC = 2
    }

}
