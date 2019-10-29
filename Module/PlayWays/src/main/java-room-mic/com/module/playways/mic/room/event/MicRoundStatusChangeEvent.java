package com.module.playways.mic.room.event;

import com.module.playways.mic.room.model.MicRoundInfoModel;

/**
 * 一场到底轮次内状态切换，由抢唱阶段变为演唱阶段
 */
public class MicRoundStatusChangeEvent {
    public int oldStatus;
    public MicRoundInfoModel roundInfo;

    public MicRoundStatusChangeEvent(MicRoundInfoModel roundInfo, int oldStatus) {
        this.roundInfo = roundInfo;
        this.oldStatus = oldStatus;
    }

    @Override
    public String toString() {
        return "MicRoundStatusChangeEvent{" +
                "oldStatus=" + oldStatus +
                ", roundInfo=" + roundInfo +
                '}';
    }
}
