package com.module.playways.battle.room.event

import com.module.playways.battle.room.model.BattleRoundInfoModel

/**
 * 由第一轮切换到第二轮
 */
class BattleRoundChangeEvent(var lastRound: BattleRoundInfoModel?, var newRound: BattleRoundInfoModel?) {

    override fun toString(): String {
        return "BattleRoundChangeEvent{" +
                "lastRoundInfo=" + lastRound +
                " newRoundInfo=" + newRound +
                '}'
    }
}
