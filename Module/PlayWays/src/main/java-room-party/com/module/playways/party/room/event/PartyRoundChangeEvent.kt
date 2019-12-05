package com.module.playways.party.room.event

import com.module.playways.mic.room.model.MicRoundInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.module.playways.relay.room.model.RelayRoundInfoModel

/**
 * 由第一轮切换到第二轮
 */
class PartyRoundChangeEvent(var lastRound: PartyRoundInfoModel?, var newRound: PartyRoundInfoModel?) {

    override fun toString(): String {
        return "PartyRoundChangeEvent{" +
                "lastRoundInfo=" + lastRound +
                " newRoundInfo=" + newRound +
                '}'
    }
}
