package com.module.playways.mic.room.event;

import com.module.playways.mic.room.model.MicRoundInfoModel;

/**
 * 一唱到底轮次切换
 * 由第一轮切换到第二轮
 */
public class MicRoundChangeEvent {
    public MicRoundInfoModel lastRoundInfo;
    public MicRoundInfoModel newRoundInfo;

    public MicRoundChangeEvent(MicRoundInfoModel lastRoundInfo, MicRoundInfoModel newRoundInfo) {
        this.lastRoundInfo = lastRoundInfo;
        this.newRoundInfo = newRoundInfo;
    }

    @Override
    public String toString() {
        return "MicRoundChangeEvent{" +
                "lastRoundInfo=" + lastRoundInfo +
                "\nnewRoundInfo=" + newRoundInfo +
                '}';
    }
}
