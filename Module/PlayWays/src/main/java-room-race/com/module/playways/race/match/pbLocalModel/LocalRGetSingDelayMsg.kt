package com.module.playways.race.match.pbLocalModel

import com.zq.live.proto.RaceRoom.RGetSingDelay
import java.io.Serializable

class LocalRGetSingDelayMsg : Serializable {
    var roundNum: Int = 0 //轮次数
    var delayTimeMs: Int = 0 //延迟时间（毫秒）

    companion object {
        fun toLocalMsgList(msgList: List<RGetSingDelay>?): List<LocalRGetSingDelayMsg> {
            var list = ArrayList<LocalRGetSingDelayMsg>()
            msgList?.forEach {
                list.add(toLocalMsg(it))
            }

            return list
        }

        fun toLocalMsg(msg: RGetSingDelay): LocalRGetSingDelayMsg {
            val localMsg = LocalRGetSingDelayMsg().apply {
                roundNum = msg.roundNum
                delayTimeMs = msg.delayTimeMs
            }

            return localMsg
        }
    }

    override fun toString(): String {
        return "LocalRGetSingDelayMsg(roundNum=$roundNum, delayTimeMs=$delayTimeMs)"
    }
}