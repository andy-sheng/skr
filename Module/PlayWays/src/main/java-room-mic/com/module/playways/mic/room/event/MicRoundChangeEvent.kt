package com.module.playways.mic.room.event

import com.module.playways.mic.room.model.MicRoundInfoModel

/**
 * 一唱到底轮次切换
 * 由第一轮切换到第二轮
 */
class MicRoundChangeEvent(var lastRound: MicRoundInfoModel?, var newRound: MicRoundInfoModel?) {

    override fun toString(): String {
        return "MicRoundChangeEvent{" +
                "lastRoundInfo=" + lastRound +
                "\nnewRoundInfo=" + newRound +
                '}'.toString()
    }
}
