package com.module.playways.mic.room.ui

import com.module.playways.mic.room.model.MicPlayerInfoModel
import com.module.playways.mic.room.model.MicRoundInfoModel

interface IMicRoomView {
    fun showWaiting()
    fun singBySelf(hasLastRound:Boolean,singCardShowListener:()->Unit)
    fun singByOthers(hasLastRound:Boolean)
    fun joinNotice(model: MicPlayerInfoModel?)
    fun kickBySomeOne(b: Boolean)
    fun dismissKickDialog()
    fun gameOver()
    fun showRoundOver(lastRoundInfo:MicRoundInfoModel?,continueOp:(()->Unit)?)
    fun receiveScoreEvent(score: Int, lineNum: Int)
    fun showSongCount(count: Int)
//    fun showWaiting(showAnimation: Boolean) // 是否需要入场动画
//    fun showChoiceView(showNextRound: Boolean)// 是否要显示下一句
//    fun showMatchAnimationView(overListener: ()->Unit)// 是否要显示下一句
//    fun showRoundOver(lastRoundInfo:RaceRoundInfoModel,continueOp:(()->Unit)?)
//    fun singBySelfFirstRound(songModel: SongModel?)
//    fun singByOtherFirstRound(songModel: SongModel?, userModel: UserInfoModel?)
//    fun singBySelfSecondRound(songModel: SongModel?)
//    fun singByOtherSecondRound(songModel: SongModel?, userModel: UserInfoModel?)
//    fun goResultPage(lastRound: RaceRoundInfoModel)
//    fun joinNotice(playerInfoModel: UserInfoModel?)
//    fun gameOver(lastRound: RaceRoundInfoModel?)
}