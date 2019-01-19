package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.RoundInfoModel;

public class GrabRoundChangeEvent {
    public RoundInfoModel lastRoundInfo;
    public RoundInfoModel newRoundInfo;
    public GrabRoundChangeEvent(RoundInfoModel lastRoundInfo,RoundInfoModel newRoundInfo) {
        this.lastRoundInfo = lastRoundInfo;
        this.newRoundInfo = newRoundInfo;
    }
}
