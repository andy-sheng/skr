package com.module.playways.race.room.ui

import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.room.song.model.SongModel

interface IRaceRoomView {
    fun showWaiting(showAnimation: Boolean) // 是否需要入场动画
    fun showChoiceView(showNextRound: Boolean)// 是否要显示下一句
    fun showMatchAnimationView(overListener: ()->Unit)// 是否要显示下一句
    fun showRoundOver(lastRoundInfo:RaceRoundInfoModel,continueOp:(()->Unit)?)
    fun singBySelfFirstRound(songModel: SongModel?)
    fun singByOtherFirstRound(songModel: SongModel?, userModel: UserInfoModel?)
    fun singBySelfSecondRound(songModel: SongModel?)
    fun singByOtherSecondRound(songModel: SongModel?, userModel: UserInfoModel?)
    fun goResultPage(lastRound: RaceRoundInfoModel)
    fun joinNotice(playerInfoModel: UserInfoModel?)
    fun gameOver(lastRound: RaceRoundInfoModel?)
}