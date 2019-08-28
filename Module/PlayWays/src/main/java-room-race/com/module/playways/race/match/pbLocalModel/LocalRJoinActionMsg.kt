package com.module.playways.race.match.pbLocalModel

import com.zq.live.proto.RaceRoom.RJoinActionMsg

class LocalRJoinActionMsg {
    var gameID: Int = 0
    var createTimeMs: Long = 0
    var agoraToken: String = ""

    constructor(gameID: Int, createTimeMs: Long, agoraToken: String) {
        this.gameID = gameID
        this.createTimeMs = createTimeMs
        this.agoraToken = agoraToken
    }

    companion object {
        fun toLocalModel(msg: RJoinActionMsg?): LocalRJoinActionMsg? {
            msg?.let {
                return LocalRJoinActionMsg(it.gameID, it.createTimeMs, it.agoraToken)
            }

            return null
        }
    }
}