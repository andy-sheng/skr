package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.RoundInfoModel;

public class GrabGameOverEvent {
    public RoundInfoModel lastRoundInfo;

    public GrabGameOverEvent(RoundInfoModel lastRoundInfo) {
        this.lastRoundInfo = lastRoundInfo;
    }
}
