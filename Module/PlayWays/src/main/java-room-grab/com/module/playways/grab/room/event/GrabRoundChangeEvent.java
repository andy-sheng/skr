package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabRoundInfoModel;

/**
 * 一唱到底轮次切换
 * 由第一轮切换到第二轮
 */
public class GrabRoundChangeEvent {
    public GrabRoundInfoModel lastRoundInfo;
    public GrabRoundInfoModel newRoundInfo;

    public GrabRoundChangeEvent(GrabRoundInfoModel lastRoundInfo, GrabRoundInfoModel newRoundInfo) {
        this.lastRoundInfo = lastRoundInfo;
        this.newRoundInfo = newRoundInfo;
    }

    @Override
    public String toString() {
        return "GrabRoundChangeEvent{" +
                "lastRoundInfo=" + lastRoundInfo +
                "\nnewRoundInfo=" + newRoundInfo +
                '}';
    }
}
