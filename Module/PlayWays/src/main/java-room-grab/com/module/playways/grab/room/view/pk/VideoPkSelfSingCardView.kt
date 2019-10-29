package com.module.playways.grab.room.view.pk

import android.view.View
import android.view.ViewStub

import com.common.log.MyLog
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView
import com.module.playways.room.data.H

/**
 * 你的主场景歌词
 */
class VideoPkSelfSingCardView(viewStub: ViewStub) : SelfSingLyricView(viewStub) {

    internal var mOverListener: (()->Unit)?=null

    override fun layoutDesc(): Int {
        return R.layout.grab_video_pk_self_sing_card_stub_layout
    }

    fun playLyric() {
        val infoModel = H.grabRoomData?.realRoundInfo
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的")
            return
        }
        tryInflate()
        setVisibility(View.VISIBLE)
        if (infoModel!!.getMusic() == null) {
            MyLog.d(TAG, "songModel 是空的")
            return
        }

        val totalTs = infoModel!!.getSingTotalMs()
        var withAcc = false
        if (infoModel!!.isAccRound() && H.grabRoomData?.isAccEnable == true || infoModel!!.isPKRound()) {
            withAcc = true
        }
        if (!withAcc) {
            playWithNoAcc(H.getSongModel())
        } else {
            playWithAcc(H.getSongModel(), totalTs)
        }
    }
}
