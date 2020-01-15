package com.module.playways.party.room.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import com.common.utils.dp
import com.module.playways.party.room.PartyRoomActivity
import java.util.ArrayList

class PartyBottomWidgetAnimationController(internal var mF: PartyRoomActivity) {

    var openType = OPEN_TYPE_SETTING
    var isOpen = false
        internal set
    private var mMainAnimatorSet: AnimatorSet? = null

    companion object {
        const val OPEN_TYPE_SETTING = 1
        const val OPEN_TYPE_EMOJI = 2
    }

    private val translateByOpenType: Int
        get() {
            return (-100).dp()
        }

    fun open(type: Int) {
        this.openType = type
        if (!isOpen) {
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
                    objectAnimator = ObjectAnimator.ofFloat<View>(view, View.TRANSLATION_Y, view.translationY, translateByOpenType.toFloat())
                    animators.add(objectAnimator)
                }
            }
            mMainAnimatorSet = AnimatorSet()
            mMainAnimatorSet?.playTogether(animators)
            mMainAnimatorSet?.duration = 300
            mMainAnimatorSet?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    showBottomView()
                    isOpen = true
                }
            })
            mMainAnimatorSet?.start()
        } else {
            showBottomView()
        }
    }

    private fun showBottomView() {
        mF.mBottomContainerView.showBackground(true)
        if (openType == OPEN_TYPE_EMOJI) {
            mF.mBottomContainerView.emojiOpen = true
            mF.mBottomContainerView.settingOpen = false
            mF.mPartySettingView?.setVisibility(View.GONE)
            mF.mPartyEmojiView?.setVisibility(View.VISIBLE)
            mF.mPartyEmojiView?.bindData()
        } else if (openType == OPEN_TYPE_SETTING) {
            mF.mBottomContainerView.settingOpen = true
            mF.mBottomContainerView.emojiOpen = false
            mF.mPartyEmojiView?.setVisibility(View.GONE)
            mF.mPartySettingView?.setVisibility(View.VISIBLE)
            mF.mPartySettingView?.bindData()
        }
    }

    private fun fillView(viewList: MutableList<View?>) {
        viewList.add(mF.mBottomContainerView)
    }

    /**
     * 使得主区域下移到 view 的下方
     */
    fun close(type: Int) {
        this.openType = type
        if (isOpen) {
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
            mMainAnimatorSet?.playTogether(animators)
            mMainAnimatorSet?.duration = 300
            mMainAnimatorSet?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    mF.mBottomContainerView.showBackground(false)
                    mF.mPartySettingView?.setVisibility(View.GONE)
                    mF.mPartyEmojiView?.setVisibility(View.GONE)
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    mF.mBottomContainerView.settingOpen = false
                    mF.mBottomContainerView.emojiOpen = false
                    isOpen = false
                }
            })
            mMainAnimatorSet!!.start()
        }
    }

    fun destroy() {
        if (mMainAnimatorSet != null) {
            mMainAnimatorSet!!.cancel()
        }
    }

}