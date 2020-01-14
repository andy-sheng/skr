package com.module.playways.party.room.ui

import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.zq.live.proto.PartyRoom.PBeginVote

interface IPartyRoomView {

    fun showRoundOver(lastRoundInfo: PartyRoundInfoModel?, continueOp:(()->Unit)?)
//    fun singPrepare(lastRoundInfo:RelayRoundInfoModel?,singCardShowListener:()->Unit)
    fun gameBegin(lastRoundInfo: PartyRoundInfoModel?)
//    fun gameOver()
    fun showWaiting()

    fun joinNotice(model: PartyPlayerInfoModel?)

    fun gameOver()

    fun showWarningDialog(warningMsg: String)

    fun showVoteView(event: PBeginVote)
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