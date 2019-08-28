package com.module.playways.race.match.model

import com.module.playways.race.room.model.RaceConfigModel
import com.module.playways.race.room.model.RaceGamePlayInfo
import com.module.playways.race.room.model.RaceRoundInfoModel
import java.io.Serializable

class JoinRaceRoomRspModel:Serializable{
    var roomID = 0
    var config: RaceConfigModel?=null
    var currentRound:RaceRoundInfoModel?=null
    var gameCreateTimeMs = 0L // 绝对事件，这个房间创建的绝对事件，以后任何事件都是以这个为基准的相对时间
    var elapsedTimeMs = 0
    var gameStartTimeMs = 0L
    var games:ArrayList<RaceGamePlayInfo>?=null
    var newRoundBegin = false
    var agoraToken:String?=null
}