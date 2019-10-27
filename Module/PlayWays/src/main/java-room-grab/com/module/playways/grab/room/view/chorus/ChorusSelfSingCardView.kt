package com.module.playways.grab.room.view.chorus

import android.view.View
import android.view.ViewStub

import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.model.ChorusRoundInfoModel
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.room.data.H


/**
 * 合唱的歌唱者看到的板子
 */
class ChorusSelfSingCardView(viewStub: ViewStub) : BaseChorusSelfCardView(viewStub) {

    internal var mSingCountDownView: SingCountDownView2? = null

    override val isForVideo: Boolean
        get() = false

    override fun init(parentView: View) {
        super.init(parentView)
        mSingCountDownView = mParentView!!.findViewById(R.id.sing_count_down_view)
        mSingCountDownView?.setListener(mOverListener)
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_chorus_self_sing_card_stub_layout
    }

    public override fun playLyric(): Boolean {
        if (super.playLyric()) {
            if (H.isGrabRoom()) {
                mSingCountDownView?.startPlay(0, H.grabRoomData!!.realRoundInfo!!.singTotalMs, true)
            } else if (H.isMicRoom()) {
                mSingCountDownView?.startPlay(0, H.micRoomData!!.realRoundInfo!!.singTotalMs, true)
            }
            return true
        } else {
            return false
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            mSingCountDownView?.reset()
        }
    }
}
