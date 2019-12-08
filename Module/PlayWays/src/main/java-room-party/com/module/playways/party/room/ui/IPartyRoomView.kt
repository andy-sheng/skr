package com.module.playways.party.room.ui

import com.module.playways.party.room.model.PartyRoundInfoModel

interface IPartyRoomView {

    fun showRoundOver(lastRoundInfo: PartyRoundInfoModel?, continueOp:(()->Unit)?)
//    fun singPrepare(lastRoundInfo:RelayRoundInfoModel?,singCardShowListener:()->Unit)
    fun gameBegin()
//    fun gameOver()
    fun showWaiting()
//    fun turnChange()

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