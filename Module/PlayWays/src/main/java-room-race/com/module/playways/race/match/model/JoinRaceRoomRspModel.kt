package com.module.playways.race.match.model

import com.module.playways.race.room.model.RaceConfigModel
import com.module.playways.race.room.model.RaceGameInfo
import com.module.playways.race.room.model.RaceRoundInfoModel

class JoinRaceRoomRspModel {
    var roomID = 0
    var config: RaceConfigModel?=null
    var currentRound:RaceRoundInfoModel?=null
    var elapsedTimeMs = 0
    var gameStartTimeMs = 0
    var games:List<RaceGameInfo>?=null
}