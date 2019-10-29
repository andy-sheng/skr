package com.module.playways.grab.room.view.normal

import android.view.View
import android.view.ViewStub
import com.common.view.ExViewStub
import com.module.playways.R
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView
import com.module.playways.room.data.H
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.GrabRoom.EQRoundStatus
import com.zq.live.proto.MicRoom.EMRoundStatus

/**
 * 你的主场景歌词
 */
class NormalSelfSingCardView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val TAG = "NormalSelfSingCardView"

    internal var mSelfSingLyricView: SelfSingLyricView? = null
    internal var mSingCountDownView: SingCountDownView2? = null

    var mOverListener: (() -> Unit)? = null

    override fun init(parentView: View) {
        run {
            val viewStub = mParentView!!.findViewById<ViewStub>(R.id.self_sing_lyric_view_stub)
            mSelfSingLyricView = SelfSingLyricView(viewStub)
        }
        mSingCountDownView = mParentView!!.findViewById(R.id.sing_count_down_view)
        mSingCountDownView!!.setListener(mOverListener)
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_normal_self_sing_card_stub_layout
    }

    override fun onViewAttachedToWindow(v: View) {
        super.onViewAttachedToWindow(v)
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
    }

    fun playLyric() {
        tryInflate()

        val totalTs = getTotalMs()
        val songModel = H.getSongModel()
        if (isAccRound() && songModel?.acc?.isNotEmpty() == true) {
            mSelfSingLyricView!!.playWithAcc(songModel, totalTs)
        } else {
            mSelfSingLyricView!!.playWithNoAcc(songModel)
        }
        mSingCountDownView!!.startPlay(0, totalTs, true)
    }

    private fun getTotalMs(): Int {
        var total: Int? = null
        if (H.isGrabRoom()) {
            total = H.grabRoomData?.realRoundInfo?.singTotalMs
        } else if (H.isMicRoom()) {
            total = H.micRoomData?.realRoundInfo?.singTotalMs
        }
        return total ?: 0
    }

    private fun isAccRound(): Boolean {
        if (H.isGrabRoom()) {
            return H.grabRoomData?.realRoundInfo?.isAccRound == true
        } else if (H.isMicRoom()) {
            return H.micRoomData?.realRoundInfo?.isAccRound == true
        }
        return false
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            if (mSingCountDownView != null) {
                mSingCountDownView!!.reset()
            }
            if (mSelfSingLyricView != null) {
                mSelfSingLyricView!!.reset()
            }
        }
    }

    fun destroy() {
        if (mSelfSingLyricView != null) {
            mSelfSingLyricView!!.destroy()
        }
    }

}
