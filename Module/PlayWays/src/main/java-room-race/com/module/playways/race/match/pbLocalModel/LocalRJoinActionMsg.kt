package com.module.playways.race.match.pbLocalModel

import com.zq.live.proto.RaceRoom.RJoinActionMsg
import java.io.Serializable

class LocalRJoinActionMsg : Serializable {
    var gameID: Int = 0
    var createTimeMs: Long = 0
    var agoraToken: String = ""


    companion object {
        fun toLocalModel(msg: RJoinActionMsg?): LocalRJoinActionMsg? {
            msg?.let {
                return LocalRJoinActionMsg().apply {
                    this.gameID = it.gameID
                    this.createTimeMs = it.createTimeMs
                    this.agoraToken = it.agoraToken
                }
            }
            return null
        }
    }

    override fun toString(): String {
        return "LocalRJoinActionMsg(gameID=$gameID, createTimeMs=$createTimeMs, agoraToken='$agoraToken')"
    }
}