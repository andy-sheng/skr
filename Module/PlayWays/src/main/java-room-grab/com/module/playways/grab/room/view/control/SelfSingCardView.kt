package com.module.playways.grab.room.view.control

import android.view.View
import android.view.ViewStub

import com.module.playways.RoomDataUtils
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.view.chorus.ChorusSelfSingCardView
import com.module.playways.grab.room.view.freemic.FreeMicSelfSingCardView
import com.module.playways.grab.room.view.minigame.MiniGameSelfSingCardView
import com.module.playways.grab.room.view.normal.NormalSelfSingCardView
import com.module.playways.grab.room.view.pk.PKSelfSingCardView
import com.module.playways.R
import com.module.playways.room.data.H

import java.util.ArrayList

class SelfSingCardView(mRootView: View) {

    internal var mNormalSelfSingCardView: NormalSelfSingCardView?=null     // 自己唱歌卡片效果
    internal var mChorusSelfSingCardView: ChorusSelfSingCardView?=null     // 合唱自己唱歌卡片效果
    internal var mPKSelfSingCardView: PKSelfSingCardView?=null             // pk自己唱歌
    internal var mMiniGameSelfSingView: MiniGameSelfSingCardView?=null     // 小游戏时卡片效果
    internal var mFreeMicSelfSingCardView: FreeMicSelfSingCardView?=null  // 自由麦

    val realViews: List<View?>
        get() {
            val list = ArrayList<View?>()
            list.add(mNormalSelfSingCardView?.realView)
            list.add(mChorusSelfSingCardView?.realView)
            list.add(mPKSelfSingCardView?.realView)
            list.add(mMiniGameSelfSingView?.realView)
            list.add(mFreeMicSelfSingCardView?.realView)
            return list
        }

    init {
        run {
            val viewStub = mRootView.findViewById<ViewStub>(R.id.normal_self_sing_card_view_stub)
            mNormalSelfSingCardView = NormalSelfSingCardView(viewStub)
        }
        run {
            val viewStub = mRootView.findViewById<ViewStub>(R.id.chorus_self_sing_card_view_stub)
            mChorusSelfSingCardView = ChorusSelfSingCardView(viewStub)
        }
        run {
            val viewStub = mRootView.findViewById<ViewStub>(R.id.pk_self_sing_card_view_stub)
            mPKSelfSingCardView = PKSelfSingCardView(viewStub)
        }
        if (H.isGrabRoom()) {
            run {
                val viewStub = mRootView.findViewById<ViewStub>(R.id.mini_game_self_sing_card_view_stub)
                mMiniGameSelfSingView = MiniGameSelfSingCardView(viewStub, H.grabRoomData)
            }
            run {
                val viewStub = mRootView.findViewById<ViewStub>(R.id.free_mic_self_sing_card_view_stub)
                mFreeMicSelfSingCardView = FreeMicSelfSingCardView(viewStub, H.grabRoomData)
            }
        }
    }

    fun setVisibility(visibility: Int) {
        if (visibility == View.GONE) {
            mNormalSelfSingCardView?.setVisibility(View.GONE)
            mChorusSelfSingCardView?.setVisibility(View.GONE)
            mPKSelfSingCardView?.setVisibility(View.GONE)
            mMiniGameSelfSingView?.setVisibility(View.GONE)
            mFreeMicSelfSingCardView?.setVisibility(View.GONE)
        } else if (visibility == View.VISIBLE) {
            if (H.isGrabRoom()) {
                when {
                    H.grabRoomData!!.realRoundInfo?.isChorusRound == true -> {
                        mChorusSelfSingCardView?.setVisibility(View.VISIBLE)
                        mNormalSelfSingCardView?.setVisibility(View.GONE)
                        mPKSelfSingCardView?.setVisibility(View.GONE)
                        mMiniGameSelfSingView?.setVisibility(View.GONE)
                        mFreeMicSelfSingCardView?.setVisibility(View.GONE)
                    }
                    H.grabRoomData!!.realRoundInfo?.isPKRound == true -> {
                        mPKSelfSingCardView?.setVisibility(View.VISIBLE)
                        mNormalSelfSingCardView?.setVisibility(View.GONE)
                        mChorusSelfSingCardView?.setVisibility(View.GONE)
                        mMiniGameSelfSingView?.setVisibility(View.GONE)
                        mFreeMicSelfSingCardView?.setVisibility(View.GONE)
                    }
                    H.grabRoomData!!.realRoundInfo?.isMiniGameRound == true -> {
                        mMiniGameSelfSingView?.setVisibility(View.VISIBLE)
                        mNormalSelfSingCardView?.setVisibility(View.GONE)
                        mChorusSelfSingCardView?.setVisibility(View.GONE)
                        mPKSelfSingCardView?.setVisibility(View.GONE)
                        mFreeMicSelfSingCardView?.setVisibility(View.GONE)
                    }
                    H.grabRoomData!!.realRoundInfo?.isFreeMicRound == true -> {
                        mMiniGameSelfSingView?.setVisibility(View.GONE)
                        mNormalSelfSingCardView?.setVisibility(View.GONE)
                        mChorusSelfSingCardView?.setVisibility(View.GONE)
                        mPKSelfSingCardView?.setVisibility(View.GONE)
                        mFreeMicSelfSingCardView?.setVisibility(View.VISIBLE)
                    }
                    else -> {
                        mNormalSelfSingCardView?.setVisibility(View.VISIBLE)
                        mChorusSelfSingCardView?.setVisibility(View.GONE)
                        mPKSelfSingCardView?.setVisibility(View.GONE)
                        mMiniGameSelfSingView?.setVisibility(View.GONE)
                        mFreeMicSelfSingCardView?.setVisibility(View.GONE)
                    }
                }
            } else if (H.isMicRoom()) {
                when {
                    H.micRoomData!!.realRoundInfo?.isChorusRound == true -> {
                        mChorusSelfSingCardView?.setVisibility(View.VISIBLE)
                        mNormalSelfSingCardView?.setVisibility(View.GONE)
                        mPKSelfSingCardView?.setVisibility(View.GONE)
                        mMiniGameSelfSingView?.setVisibility(View.GONE)
                        mFreeMicSelfSingCardView?.setVisibility(View.GONE)
                    }
                    H.micRoomData!!.realRoundInfo?.isPKRound == true -> {
                        mPKSelfSingCardView?.setVisibility(View.VISIBLE)
                        mNormalSelfSingCardView?.setVisibility(View.GONE)
                        mChorusSelfSingCardView?.setVisibility(View.GONE)
                        mMiniGameSelfSingView?.setVisibility(View.GONE)
                        mFreeMicSelfSingCardView?.setVisibility(View.GONE)
                    }
                    else -> {
                        mNormalSelfSingCardView?.setVisibility(View.VISIBLE)
                        mChorusSelfSingCardView?.setVisibility(View.GONE)
                        mPKSelfSingCardView?.setVisibility(View.GONE)
                        mMiniGameSelfSingView?.setVisibility(View.GONE)
                        mFreeMicSelfSingCardView?.setVisibility(View.GONE)
                    }
                }
            }
        }
    }

    fun playLyric() {
        if (H.isGrabRoom()) {
            when {
                H.grabRoomData!!.realRoundInfo?.isChorusRound == true -> mChorusSelfSingCardView?.playLyric()
                H.grabRoomData!!.realRoundInfo?.isPKRound == true -> mPKSelfSingCardView?.playLyric()
                H.grabRoomData!!.realRoundInfo?.isMiniGameRound == true -> mMiniGameSelfSingView?.playLyric()
                H.grabRoomData!!.realRoundInfo?.isFreeMicRound == true -> mFreeMicSelfSingCardView?.playLyric()
                else -> mNormalSelfSingCardView?.playLyric()
            }
        } else {
            when {
                H.micRoomData!!.realRoundInfo?.isChorusRound == true -> mChorusSelfSingCardView?.playLyric()
                H.micRoomData!!.realRoundInfo?.isPKRound == true -> mPKSelfSingCardView?.playLyric()
                else -> mNormalSelfSingCardView?.playLyric()
            }
        }
    }

    fun destroy() {
        mNormalSelfSingCardView?.destroy()
        mChorusSelfSingCardView?.destroy()
        mPKSelfSingCardView?.destroy()
        mMiniGameSelfSingView?.destroy()
        mFreeMicSelfSingCardView?.destroy()
    }

    fun setListener(overlListener: ()->Unit) {
        mNormalSelfSingCardView?.mOverListener = overlListener
        mChorusSelfSingCardView?.mOverListener = overlListener
        mPKSelfSingCardView?.mOverListener = overlListener
        mMiniGameSelfSingView?.mOverListener = overlListener
    }

    fun setListener4FreeMic(overlListener: ()->Unit) {
        mFreeMicSelfSingCardView?.mOverListener = overlListener
    }

    fun setTranslateY(ty: Float) {
        mChorusSelfSingCardView?.setTranslateY(ty)
        mPKSelfSingCardView?.setTranslateY(ty)
        mMiniGameSelfSingView?.setTranslateY(ty)
        mNormalSelfSingCardView?.setTranslateY(ty)
        mFreeMicSelfSingCardView?.setTranslateY(ty)
    }

}
