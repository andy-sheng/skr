package com.module.playways.race.room.model

import com.zq.live.proto.RaceRoom.RWantSingInfo

class RaceWantSingInfo {
    var userID = 0
    var choiceID = 0
    var timeMs = 0L
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RaceWantSingInfo

        if (userID != other.userID) return false

        return true
    }

    override fun hashCode(): Int {
        return userID
    }

}

fun parseFromWantSingInfoPB(pb: RWantSingInfo): RaceWantSingInfo {
    val model = RaceWantSingInfo()
    model.userID = pb.userID
    model.choiceID = pb.choiceID
    model.timeMs = pb.timeMs
    return model
}