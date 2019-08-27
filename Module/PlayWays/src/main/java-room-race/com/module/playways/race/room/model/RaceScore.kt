package com.module.playways.race.room.model

import com.zq.live.proto.RaceRoom.RoundScoreInfo

class RaceScore{
    var bLightCnt = 0
    var isEscape = false
    var winType = 0
}
internal fun parseFromRoundScoreInfoPB(pb: RoundScoreInfo):RaceScore {
    val model = RaceScore()
    model.bLightCnt = pb.bLightCnt
    model.isEscape = pb.isEscape
    model.winType = pb.winType.value
    return model
}