package com.module.playways.grab.room.view.control

import android.view.View
import android.view.ViewGroup
import android.view.ViewStub

import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.U
import com.module.playways.RoomDataUtils
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.listener.SVGAListener
import com.module.playways.grab.room.model.ChorusRoundInfoModel
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.model.MINIGameRoundInfoModel
import com.module.playways.grab.room.model.SPkRoundInfoModel
import com.common.view.ExViewStub
import com.module.playways.grab.room.view.chorus.ChorusSingBeginTipsCardView
import com.module.playways.grab.room.view.minigame.MiniGameSingBeginTipsCardView
import com.module.playways.grab.room.view.normal.NormalSingBeginTipsCardView
import com.module.playways.grab.room.view.pk.PKSingBeginTipsCardView
import com.module.playways.R
import com.opensource.svgaplayer.SVGAImageView

class SingBeginTipsCardView(viewStub: ViewStub, internal var mRoomData: GrabRoomData) : ExViewStub(viewStub) {

    internal var mNormalSingBeginTipsCardView = NormalSingBeginTipsCardView() // 提示xxx演唱开始的卡片
    internal var mChorusSingBeginTipsCardView = ChorusSingBeginTipsCardView() // 合唱对战开始
    internal var mPKSingBeginTipsCardView = PKSingBeginTipsCardView()         // pk对战开始
    internal var mMiniGameSingBegin = MiniGameSingBeginTipsCardView()         // 小游戏开始

    internal var mSVGAImageView: SVGAImageView? = null

    override fun init(parentView: View) {
        mSVGAImageView = mParentView!!.findViewById(R.id.sing_begin_svga)
        mParentView!!.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {

            }

            override fun onViewDetachedFromWindow(v: View) {
                if (mSVGAImageView != null) {
                    mSVGAImageView!!.callback = null
                    mSVGAImageView!!.stopAnimation(true)
                }
            }
        })
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_sing_begin_tips_card_stub_layout
    }


    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            if (mSVGAImageView != null) {
                mSVGAImageView!!.callback = null
                mSVGAImageView!!.stopAnimation(true)
            }
        }
    }

    fun bindData(svgaListener: SVGAListener) {
        val grabRoundInfoModel = mRoomData.realRoundInfo
        tryInflate()
        setVisibility(View.VISIBLE)
        mSVGAImageView!!.visibility = View.VISIBLE
        if (grabRoundInfoModel != null) {
            if (mRoomData?.realRoundInfo?.isChorusRound == true) {
                val list = grabRoundInfoModel.chorusRoundInfoModels
                if (list != null && list.size >= 2) {
                    val userInfoModel1 = mRoomData.getPlayerOrWaiterInfo(list[0].userID)
                    val userInfoModel2 = mRoomData.getPlayerOrWaiterInfo(list[1].userID)
                    val lp = mSVGAImageView!!.layoutParams
                    lp.height = U.getDisplayUtils().dip2px(154f)
                    mChorusSingBeginTipsCardView.bindData(mSVGAImageView, userInfoModel1, userInfoModel2, svgaListener)
                }
            } else if (mRoomData?.realRoundInfo?.isPKRound == true) {
                val list = grabRoundInfoModel.getsPkRoundInfoModels()
                if (list != null && list.size >= 2) {
                    val userInfoModel1 = mRoomData.getPlayerOrWaiterInfo(list[0].userID)
                    val userInfoModel2 = mRoomData.getPlayerOrWaiterInfo(list[1].userID)
                    val lp = mSVGAImageView!!.layoutParams
                    lp.height = U.getDisplayUtils().dip2px(181f)
                    mPKSingBeginTipsCardView.bindData(mSVGAImageView, userInfoModel1, userInfoModel2, svgaListener)
                }
            } else if (mRoomData?.realRoundInfo?.isMiniGameRound == true) {
                val list = grabRoundInfoModel.miniGameRoundInfoModels
                if (list != null && list.size >= 2) {
                    val userInfoModel1 = mRoomData.getPlayerOrWaiterInfo(list[0].userID)
                    val userInfoModel2 = mRoomData.getPlayerOrWaiterInfo(list[1].userID)
                    val lp = mSVGAImageView!!.layoutParams
                    lp.height = U.getDisplayUtils().dip2px(154f)
                    mMiniGameSingBegin.bindData(mSVGAImageView, userInfoModel1, userInfoModel2, svgaListener)
                }
            } else {
                val lp = mSVGAImageView!!.layoutParams
                lp.height = U.getDisplayUtils().dip2px(181f)
                mNormalSingBeginTipsCardView.bindData(mSVGAImageView, mRoomData.getPlayerOrWaiterInfo(grabRoundInfoModel.userID), grabRoundInfoModel.music, svgaListener, grabRoundInfoModel.isChallengeRound)
            }
        }
    }


}
