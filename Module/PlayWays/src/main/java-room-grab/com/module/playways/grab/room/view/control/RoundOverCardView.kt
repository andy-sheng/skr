package com.module.playways.grab.room.view.control

import android.view.View
import android.view.ViewStub

import com.module.playways.RoomDataUtils
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.listener.SVGAListener
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.view.minigame.MiniGameRoundOverCardView
import com.module.playways.grab.room.view.normal.NormalRoundOverCardView
import com.module.playways.grab.room.view.pk.PKRoundOverCardView
import com.module.playways.R
import com.zq.live.proto.Common.StandPlayType

import java.util.ArrayList

class RoundOverCardView(mRootView: View, internal var mRoomData: GrabRoomData) {

    internal var mNormalRoundOverCardView: NormalRoundOverCardView?=null   // 轮次结束的卡片
    internal var mPKRoundOverCardView: PKRoundOverCardView?=null           // pk轮次结束卡片
    internal var mMiniGameOverCardView: MiniGameRoundOverCardView?=null    // 小游戏结束卡片

    val realViews: List<View?>
        get() {
            val list = ArrayList<View?>()
            list.add(mNormalRoundOverCardView?.realView)
            list.add(mPKRoundOverCardView?.realView)
            list.add(mMiniGameOverCardView?.realView)
            return list
        }

    init {
        run {
            val viewStub = mRootView.findViewById<ViewStub>(R.id.normal_round_over_card_view_stub)
            mNormalRoundOverCardView = NormalRoundOverCardView(viewStub)
        }

        run {
            val viewStub = mRootView.findViewById<ViewStub>(R.id.pk_round_over_card_view_stub)
            mPKRoundOverCardView = PKRoundOverCardView(viewStub, mRoomData)
        }

        run {
            val viewStub = mRootView.findViewById<ViewStub>(R.id.mini_game_over_card_view_stub)
            mMiniGameOverCardView = MiniGameRoundOverCardView(viewStub)
        }
    }

    fun bindData(lastRoundInfo: GrabRoundInfoModel?, svgaListener: SVGAListener) {
        if (lastRoundInfo != null) {
            if (lastRoundInfo.music != null && lastRoundInfo.music.playType == StandPlayType.PT_SPK_TYPE.value) {
                // 是pk的轮次 并且 两个轮次 userId 有效 ，说明有人玩了
                if (lastRoundInfo.getsPkRoundInfoModels().size >= 2) {
                    if (lastRoundInfo.getsPkRoundInfoModels()[0].userID != 0 && lastRoundInfo.getsPkRoundInfoModels()[1].userID != 0) {
                        mPKRoundOverCardView?.bindData(lastRoundInfo, svgaListener)
                        return
                    }
                }
            }
        }

        if (lastRoundInfo != null) {
            if (lastRoundInfo.music != null && lastRoundInfo.music.playType == StandPlayType.PT_MINI_GAME_TYPE.value) {
                // 是小游戏的轮次 并且 两个轮次 userId 有效 ，说明有人玩了
                if (lastRoundInfo.miniGameRoundInfoModels.size >= 2) {
                    if (lastRoundInfo.miniGameRoundInfoModels[0].userID != 0 && lastRoundInfo.miniGameRoundInfoModels[1].userID != 0) {
                        mMiniGameOverCardView?.bindData(lastRoundInfo, svgaListener)
                        return
                    }
                }
            }
        }

        mNormalRoundOverCardView?.bindData(lastRoundInfo, svgaListener)
    }

    fun setVisibility(visibility: Int) {
        if (visibility == View.GONE) {
            mNormalRoundOverCardView?.setVisibility(View.GONE)
            mPKRoundOverCardView?.setVisibility(View.GONE)
            mMiniGameOverCardView?.setVisibility(View.GONE)
        } else if (visibility == View.VISIBLE) {
            when {
                mRoomData?.realRoundInfo?.isChorusRound == true -> {
                    mNormalRoundOverCardView?.setVisibility(View.VISIBLE)
                    mPKRoundOverCardView?.setVisibility(View.GONE)
                    mMiniGameOverCardView?.setVisibility(View.GONE)
                }
                mRoomData?.realRoundInfo?.isPKRound == true -> {
                    mPKRoundOverCardView?.setVisibility(View.VISIBLE)
                    mNormalRoundOverCardView?.setVisibility(View.GONE)
                    mMiniGameOverCardView?.setVisibility(View.GONE)
                }
                mRoomData?.realRoundInfo?.isMiniGameRound == true -> {
                    mMiniGameOverCardView?.setVisibility(View.VISIBLE)
                    mNormalRoundOverCardView?.setVisibility(View.GONE)
                    mPKRoundOverCardView?.setVisibility(View.GONE)
                }
                else -> {
                    mNormalRoundOverCardView?.setVisibility(View.VISIBLE)
                    mPKRoundOverCardView?.setVisibility(View.GONE)
                    mMiniGameOverCardView?.setVisibility(View.GONE)
                }
            }
        }
    }
}
