package com.module.playways.relay.room.event

import com.module.playways.mic.room.model.MicRoundInfoModel
import com.module.playways.relay.room.model.RelayRoundInfoModel

/**
 * 一唱到底轮次切换
 * 由第一轮切换到第二轮
 */
class RelayRoundChangeEvent(var lastRound: RelayRoundInfoModel?, var newRound: RelayRoundInfoModel?) {

    override fun toString(): String {
        return "RelayRoundChangeEvent{" +
                "lastRoundInfo=" + lastRound +
                " newRoundInfo=" + newRound +
                '}'.toString()
    }
}
