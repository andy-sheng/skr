package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.RoundInfoModel;

public class GrabRoundStatusChangeEvent {
    public int oldStatus;
    public RoundInfoModel roundInfo;

    public GrabRoundStatusChangeEvent(RoundInfoModel roundInfo, int oldStatus) {
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
