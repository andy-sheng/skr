package com.module.playways.relay.room.ui

import com.module.playways.mic.room.model.MicPlayerInfoModel
import com.module.playways.mic.room.model.MicRoundInfoModel
import com.module.playways.relay.room.model.RelayRoundInfoModel

interface IRelayRoomView {
    fun showRoundOver(lastRoundInfo:RelayRoundInfoModel?,continueOp:(()->Unit)?)
    fun singPrepare(lastRoundInfo:RelayRoundInfoModel?,singCardShowListener:()->Unit)
    fun singBegin()
    fun gameOver()
    fun showWaiting()

//
//    fun singByOthers(lastRoundInfo:MicRoundInfoModel?)
//    fun joinNotice(model: MicPlayerInfoModel?)
//    fun kickBySomeOne(b: Boolean)
//    fun dismissKickDialog()

//
//    fun receiveScoreEvent(score: Int)
//    fun showSongCount(count: Int)
//    fun ensureActivtyTop()
//    fun invitedToOtherRoom()
}