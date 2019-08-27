package com.module.playways.race.room.inter

import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.room.song.model.SongModel

interface IRaceRoomView {
    fun showWaiting(showAnimation: Boolean) // 是否需要入场动画
    fun showChoicing(showNextRound: Boolean)// 是否要显示下一句
    fun singBySelfFirstRound(songModel: SongModel?)
    fun singByOtherFirstRound(songModel: SongModel?, userModel: UserInfoModel?)
    fun singBySelfSecondRound(songModel: SongModel?)
    fun singByOtherSecondRound(songModel: SongModel?, userModel: UserInfoModel?)
    fun roundOver(overReason: Int)

}