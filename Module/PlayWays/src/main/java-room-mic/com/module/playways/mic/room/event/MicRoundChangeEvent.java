package com.module.playways.mic.room.event;

import com.module.playways.mic.room.model.MicRoundInfoModel;

/**
 * 一唱到底轮次切换
 * 由第一轮切换到第二轮
 */
public class MicRoundChangeEvent {
    public MicRoundInfoModel lastRound;
    public MicRoundInfoModel newRound;

    public MicRoundChangeEvent(MicRoundInfoModel lastRoundInfo, MicRoundInfoModel newRoundInfo) {
        this.lastRound = lastRoundInfo;
        this.newRound = newRoundInfo;
    }

    @Override
    public String toString() {
        return "MicRoundChangeEvent{" +
                "lastRoundInfo=" + lastRound +
                "\nnewRoundInfo=" + newRound +
                '}';
    }
}
