package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.GrabRoundInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;

public class GrabRoundChangeEvent {
    public GrabRoundInfoModel lastRoundInfo;
    public GrabRoundInfoModel newRoundInfo;
    public GrabRoundChangeEvent(GrabRoundInfoModel lastRoundInfo,GrabRoundInfoModel newRoundInfo) {
        this.lastRoundInfo = lastRoundInfo;
        this.newRoundInfo = newRoundInfo;
    }

    @Override
    public String toString() {
        return "GrabRoundChangeEvent{" +
                "lastRoundInfo=" + lastRoundInfo +
                ", newRoundInfo=" + newRoundInfo +
                '}';
    }
}
