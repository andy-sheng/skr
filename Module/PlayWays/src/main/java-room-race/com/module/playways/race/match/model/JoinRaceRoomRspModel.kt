package com.module.playways.race.match.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.race.room.model.RaceConfigModel
import com.module.playways.race.room.model.RaceGamePlayInfo
import com.module.playways.race.room.model.RaceRoundInfoModel
import java.io.Serializable

class JoinRaceRoomRspModel : Serializable {
    @JSONField(name = "roomID")
    var roomID = 0
    @JSONField(name = "config")
    var config: RaceConfigModel? = null
    @JSONField(name = "currentRound")
    var currentRound: RaceRoundInfoModel? = null
    @JSONField(name = "gameCreateTimeMs")
    var gameCreateTimeMs = 0L // 绝对事件，这个房间创建的绝对事件，以后任何事件都是以这个为基准的相对时间
    @JSONField(name = "elapsedTimeMs")
    var elapsedTimeMs = 0
    @JSONField(name = "gameStartTimeMs")
    var gameStartTimeMs = 0L
    @JSONField(name = "games")
    var games: ArrayList<RaceGamePlayInfo>? = null
    @JSONField(name = "newRoundBegin")
    var newRoundBegin = false
    @JSONField(name = "agoraToken")
    var agoraToken: String? = null
}