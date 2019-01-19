package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.RoundInfoModel;

public class GrabRoundStatusChangeEvent {
    public RoundInfoModel roundInfo;
    public int oldStatus;
    public GrabRoundStatusChangeEvent(RoundInfoModel roundInfo, int oldStatus) {
        this.roundInfo = roundInfo;
        this.oldStatus = oldStatus;
    }
}
