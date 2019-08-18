package com.module.playways.race.room.ui

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
class RaceWidgetAnimationController(internal var mF: RaceRoomFragment) {

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
        val viewList = ArrayList<View>()
        fillView(viewList)

        val animators = ArrayList<Animator>()
        for (view in viewList) {
            if (view != null) {
                var objectAnimator: ObjectAnimator? = null
//                if (view === mF.mGrabVideoDisplayView.getRealView()) {
//                    // 要多下移一个顶部状态栏的高度，才能和 ContentView对齐
//                    objectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.translationY, translateByOpenType + mF.mGrabVideoDisplayView.getExtraTranslateYWhenOpen(openType))
//                } else {
                objectAnimator = ObjectAnimator.ofFloat<View>(view, View.TRANSLATION_Y, view.translationY, translateByOpenType.toFloat())
//                }
                animators.add(objectAnimator)
            }
        }
//        val animators2 = mF.mGrabVideoDisplayView.getInnerAnimator(true, mF.mGrabTopContentView.getVisibility() === View.VISIBLE)
//        if (animators2 != null) {
//            animators.addAll(animators2)
//        }
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
                mF.mRaceTopContentView.setArrowIcon(true)
                if (openType == OPEN_TYPE_FOR_NORMAL) {
                    mF.mRaceTopOpView.setVisibility(View.VISIBLE)
//                    mF.mRaceVideoSelfSingCardView.setVisibility(View.GONE)
                } else if (openType == OPEN_TYPE_FOR_LYRIC) {
                    mF.mRaceTopOpView.setVisibility(View.GONE)
//                    mF.mRaceVideoSelfSingCardView.setVisibility(View.VISIBLE)
                }
                isOpen = true
            }
        })
        mMainAnimatorSet!!.start()
//        mF.mGameTipsManager.setBaseTranslateY(mF.TAG_INVITE_TIP_VIEW, translateByOpenType)
    }

    internal fun fillView(viewList: MutableList<View>) {
        viewList.add(mF.mRaceTopContentView)
//        viewList.add(mF.mPracticeFlagIv)
//        viewList.add(mF.mGameTipsManager.getViewByKey(mF.TAG_SELF_SING_TIP_VIEW))
//        viewList.add(mF.mRaceOpBtn)
//        viewList.add(mF.mRaceGiveupView)
//        viewList.add(mF.mTurnInfoCardView)
//        viewList.add(mF.mSongInfoCardView)
//        viewList.addAll(mF.mRoundOverCardView.getRealViews())
//        viewList.add(mF.mGiftContinueViewGroup)
//
//        if (mF.mRoomData.isVideoRoom()) {
//            viewList.add(mF.mRaceVideoDisplayView.getRealView())
//        } else {
//            viewList.addAll(mF.mOthersSingCardView.getRealViews())
//        }
//        viewList.addAll(mF.mSelfSingCardView.getRealViews())
    }

    /**
     * 使得主区域下移到 view 的下方
     */
    fun close() {
        if (mMainAnimatorSet != null && mMainAnimatorSet!!.isRunning) {
            mMainAnimatorSet!!.cancel()
        }
        // 需要改变偏移的对象
        val viewList = ArrayList<View>()
        fillView(viewList)
        val animators = ArrayList<Animator>()
        for (view in viewList) {
            if (view != null) {
                val objectAnimator = ObjectAnimator.ofFloat<View>(view, View.TRANSLATION_Y, view.translationY, 0f)
                animators.add(objectAnimator)
            }
        }
//        val animators2 = mF.mRaceVideoDisplayView.getInnerAnimator(false, mF.mRaceTopContentView.getVisibility() === View.VISIBLE)
//        if (animators2 != null) {
//            animators.addAll(animators2)
//        }
        mMainAnimatorSet = AnimatorSet()
        mMainAnimatorSet!!.playTogether(animators)
        mMainAnimatorSet!!.duration = 300
        mMainAnimatorSet!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                mF.mRaceTopOpView.setVisibility(View.GONE)
//                mF.mRaceVideoSelfSingCardView.setVisibility(View.GONE)
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mF.mRaceTopContentView.setArrowIcon(false)
                isOpen = false
            }
        })
        mMainAnimatorSet!!.start()
//        mF.mGameTipsManager.setBaseTranslateY(mF.TAG_INVITE_TIP_VIEW, 0)

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
