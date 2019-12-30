package com.module.playways.relay.room.ui

import com.module.playways.relay.room.model.RelayRoundInfoModel

interface IRelayRoomView {
    fun showRoundOver(lastRoundInfo: RelayRoundInfoModel?, continueOp: (() -> Unit)?)
    fun singPrepare(lastRoundInfo: RelayRoundInfoModel?, singCardShowListener: () -> Unit)
    fun singBegin()
    fun gameOver(from: String)
    fun showWaiting()
    fun turnChange()
    fun turnMyChangePrepare()
    //当另一个被邀请的人进来的时候
    fun startGameByInvite()

    fun receiveScoreEvent(score: Int)

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