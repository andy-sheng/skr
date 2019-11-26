package com.module.playways.relay.room.event;

import com.module.playways.relay.room.model.RelayRoundInfoModel;

public class RelayRoundStatusChangeEvent {
    public int oldStatus;
    public RelayRoundInfoModel roundInfo;

    public RelayRoundStatusChangeEvent(RelayRoundInfoModel roundInfo, int oldStatus) {
        this.roundInfo = roundInfo;
        this.oldStatus = oldStatus;
    }

    @Override
    public String toString() {
        return "RelayRoundStatusChangeEvent{" +
                "oldStatus=" + oldStatus +
                ", roundInfo=" + roundInfo +
                '}';
    }
}
