package com.module.playways.mic.room.view.control

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
import com.module.playways.grab.room.view.chorus.ChorusSelfSingCardView
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.grab.room.view.control.SingBeginTipsCardView
import com.module.playways.grab.room.view.freemic.FreeMicSelfSingCardView
import com.module.playways.grab.room.view.minigame.MiniGameSelfSingCardView
import com.module.playways.grab.room.view.normal.NormalSelfSingCardView
import com.module.playways.grab.room.view.pk.PKSelfSingCardView
import com.module.playways.mic.room.view.normal.MicNormalSingBeginTipsCardView
import com.module.playways.room.data.H
import com.opensource.svgaplayer.SVGAImageView
import java.util.ArrayList

class MicSingBeginTipsCardView(rootView: View) {
    private var mGrabSingBeginTipsCardView: SingBeginTipsCardView? = null     // 演唱开始的提示
    private var mMicSingBeginTipsCardView: MicNormalSingBeginTipsCardView? = null     // 演唱开始的提示

    init {
        mGrabSingBeginTipsCardView = SingBeginTipsCardView(rootView.findViewById(R.id.sing_begin_tips_card_stub))
        mMicSingBeginTipsCardView = MicNormalSingBeginTipsCardView(rootView.findViewById(R.id.mic_sing_begin_tips_card_stub))
    }

    fun setVisibility(visibility: Int) {
        if (visibility == View.GONE) {
            mGrabSingBeginTipsCardView?.setVisibility(View.GONE)
            mMicSingBeginTipsCardView?.setVisibility(View.GONE)
        } else if (visibility == View.VISIBLE) {
        }
    }

    fun bindData(svgaListener: SVGAListener) {
        if (H.micRoomData?.realRoundInfo?.isNormalRound == true) {
            mMicSingBeginTipsCardView?.bindData(svgaListener)
        } else {
            mGrabSingBeginTipsCardView?.bindData(svgaListener)
        }
    }

}
