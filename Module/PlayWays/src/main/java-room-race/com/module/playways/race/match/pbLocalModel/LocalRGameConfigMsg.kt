package com.module.playways.race.match.pbLocalModel

import com.zq.live.proto.RaceRoom.RGameConfig
import java.io.Serializable

class LocalRGameConfigMsg : Serializable {
    var getSingDelay: List<LocalRGetSingDelayMsg>? = null

    companion object {
        fun toLocalMsg(config: RGameConfig): LocalRGameConfigMsg {
            return LocalRGameConfigMsg().apply {
                getSingDelay = LocalRGetSingDelayMsg.toLocalMsgList(config.getSingDelayList)
            }
        }
    }

    fun getDelayTime(runningRoundCount: Int): Long {
        getSingDelay?.let {
            it.forEach {
                if (it.roundNum == runningRoundCount) {
                    return it.delayTimeMs.toLong() / 3
                }
            }

            return it.last().delayTimeMs.toLong() / 3
        }

        return 1000
    }
}