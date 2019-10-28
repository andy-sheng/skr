package com.module.playways.grab.room.view.control

import android.view.View
import android.view.ViewStub

import com.module.playways.RoomDataUtils
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.view.chorus.ChorusOthersSingCardView
import com.module.playways.grab.room.view.minigame.MiniGameOtherSingCardView
import com.module.playways.grab.room.view.normal.NormalOthersSingCardView
import com.module.playways.grab.room.view.pk.PKOthersSingCardView
import com.module.playways.R
import com.module.playways.room.data.H

import java.util.ArrayList

class OthersSingCardView(mRootView: View) {

    internal var mNormalOthersSingCardView: NormalOthersSingCardView? = null  // 他人唱歌卡片效果
    internal var mChorusOtherSingCardView: ChorusOthersSingCardView? = null   // 合唱他人唱歌卡片效果
    internal var mPKOtherSingCardView: PKOthersSingCardView? = null          // PK他人唱歌卡片效果
    internal var mMiniGameOtherSingView: MiniGameOtherSingCardView? = null    // 小游戏卡片效果

    val realViews: List<View?>
        get() {
            val list = ArrayList<View?>()
            list.add(mChorusOtherSingCardView?.realView)
            list.add(mPKOtherSingCardView?.realView)
            list.add(mMiniGameOtherSingView?.realView)
            return list
        }

    init {
        run {
            val viewStub = mRootView.findViewById<ViewStub>(R.id.normal_other_sing_card_view_stub)
            mNormalOthersSingCardView = NormalOthersSingCardView(viewStub)
        }
        run {
            val viewStub = mRootView.findViewById<ViewStub>(R.id.chorus_other_sing_card_view_stub)
            mChorusOtherSingCardView = ChorusOthersSingCardView(viewStub)
        }
        run {
            val viewStub = mRootView.findViewById<ViewStub>(R.id.pk_other_sing_card_view_stub)
            mPKOtherSingCardView = PKOthersSingCardView(viewStub)
        }
        if (H.isGrabRoom()) {
            run {
                val viewStub = mRootView.findViewById<ViewStub>(R.id.mini_game_other_sing_card_view_stub)
                mMiniGameOtherSingView = MiniGameOtherSingCardView(viewStub, H.grabRoomData)
            }
        }
    }

    fun setVisibility(visibility: Int) {
        if (visibility == View.GONE) {
            mNormalOthersSingCardView?.setVisibility(View.GONE)
            mChorusOtherSingCardView?.setVisibility(View.GONE)
            mPKOtherSingCardView?.setVisibility(View.GONE)
            mMiniGameOtherSingView?.setVisibility(View.GONE)
        } else if (visibility == View.VISIBLE) {
            if (H.isGrabRoom()) {
                when {
                    H.grabRoomData?.realRoundInfo?.isChorusRound == true -> {
                        mChorusOtherSingCardView?.setVisibility(View.VISIBLE)
                        mPKOtherSingCardView?.setVisibility(View.GONE)
                        mNormalOthersSingCardView?.setVisibility(View.GONE)
                        mMiniGameOtherSingView?.setVisibility(View.GONE)
                    }
                    H.grabRoomData?.realRoundInfo?.isPKRound == true -> {
                        mPKOtherSingCardView?.setVisibility(View.VISIBLE)
                        mChorusOtherSingCardView?.setVisibility(View.GONE)
                        mNormalOthersSingCardView?.setVisibility(View.GONE)
                        mMiniGameOtherSingView?.setVisibility(View.GONE)
                    }
                    H.grabRoomData?.realRoundInfo?.isMiniGameRound == true -> {
                        mNormalOthersSingCardView?.setVisibility(View.GONE)
                        mChorusOtherSingCardView?.setVisibility(View.GONE)
                        mPKOtherSingCardView?.setVisibility(View.GONE)
                        mMiniGameOtherSingView?.setVisibility(View.VISIBLE)
                    }
                    else -> {
                        mNormalOthersSingCardView?.setVisibility(View.VISIBLE)
                        mChorusOtherSingCardView?.setVisibility(View.GONE)
                        mPKOtherSingCardView?.setVisibility(View.GONE)
                        mMiniGameOtherSingView?.setVisibility(View.GONE)
                    }
                }
            } else if (H.isMicRoom()) {
                when {
                    H.micRoomData?.realRoundInfo?.isChorusRound == true -> {
                        mChorusOtherSingCardView?.setVisibility(View.VISIBLE)
                        mPKOtherSingCardView?.setVisibility(View.GONE)
                        mNormalOthersSingCardView?.setVisibility(View.GONE)
                    }
                    H.micRoomData?.realRoundInfo?.isPKRound == true -> {
                        mPKOtherSingCardView?.setVisibility(View.VISIBLE)
                        mChorusOtherSingCardView?.setVisibility(View.GONE)
                        mNormalOthersSingCardView?.setVisibility(View.GONE)
                    }
                    else -> {
                        mNormalOthersSingCardView?.setVisibility(View.VISIBLE)
                        mChorusOtherSingCardView?.setVisibility(View.GONE)
                        mPKOtherSingCardView?.setVisibility(View.GONE)
                    }
                }
            }

        }
    }

    fun bindData() {
        if (H.isGrabRoom()) {
            when {
                H.grabRoomData?.realRoundInfo?.isChorusRound == true -> mChorusOtherSingCardView?.bindData()
                H.grabRoomData?.realRoundInfo?.isPKRound == true -> mPKOtherSingCardView?.bindData()
                H.grabRoomData?.realRoundInfo?.isMiniGameRound == true -> mMiniGameOtherSingView?.bindData()
                else -> mNormalOthersSingCardView?.bindData()
            }
        } else if (H.isMicRoom()) {
            when {
                H.micRoomData?.realRoundInfo?.isChorusRound == true -> mChorusOtherSingCardView?.bindData()
                H.micRoomData?.realRoundInfo?.isPKRound == true -> mPKOtherSingCardView?.bindData()
                else -> mNormalOthersSingCardView?.bindData()
            }
        }
    }

    fun tryStartCountDown() {
        mNormalOthersSingCardView?.tryStartCountDown()
        mChorusOtherSingCardView?.tryStartCountDown()
        mMiniGameOtherSingView?.tryStartCountDown()
    }

    fun hide() {
        mNormalOthersSingCardView?.hide()
        mChorusOtherSingCardView?.hide()
        mPKOtherSingCardView?.hide()
        mMiniGameOtherSingView?.hide()
    }

    fun setTranslateY(ty: Int) {
        mNormalOthersSingCardView?.setTranslateY(ty.toFloat())
        mChorusOtherSingCardView?.setTranslateY(ty.toFloat())
        mPKOtherSingCardView?.setTranslateY(ty.toFloat())
        mMiniGameOtherSingView?.setTranslateY(ty.toFloat())
    }

}
