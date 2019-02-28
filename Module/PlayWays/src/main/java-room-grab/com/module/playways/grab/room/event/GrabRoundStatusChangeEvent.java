package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabRoundInfoModel;

/**
 * 一场到底轮次内状态切换，由抢唱阶段变为演唱阶段
 */
public class GrabRoundStatusChangeEvent {
    public int oldStatus;
    public GrabRoundInfoModel roundInfo;

    public GrabRoundStatusChangeEvent(GrabRoundInfoModel roundInfo, int oldStatus) {
        this.roundInfo = roundInfo;
        this.oldStatus = oldStatus;
    }

    @Override
    public String toString() {
        return "GrabRoundStatusChangeEvent{" +
                "oldStatus=" + oldStatus +
                ", roundInfo=" + roundInfo +
                '}';
    }
}
