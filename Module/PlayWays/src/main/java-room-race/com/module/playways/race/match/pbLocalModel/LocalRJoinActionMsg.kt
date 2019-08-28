package com.module.playways.race.match.pbLocalModel

import com.zq.live.proto.RaceRoom.RJoinActionMsg

class LocalRJoinActionMsg {
    var gameID: Int = 0
    var createTimeMs: Long = 0
    var agoraToken: String = ""

    companion object {
        fun toLocalModel(msg: RJoinActionMsg?): LocalRJoinActionMsg? {
            msg?.let {
                return LocalRJoinActionMsg().apply {
                    it.gameID
                    it.createTimeMs
                    it.agoraToken
                }
            }

            return null
        }
    }
}