package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.GrabRoundInfoModel;

public class GrabGameOverEvent {
    public GrabRoundInfoModel lastRoundInfo;

    public GrabGameOverEvent(GrabRoundInfoModel lastRoundInfo) {
        this.lastRoundInfo = lastRoundInfo;
    }
}
