package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.RoundInfoModel;

public class SomeOneLightOffEvent {
    public RoundInfoModel roundInfo;
    public int uid;

    public SomeOneLightOffEvent(int uid, RoundInfoModel newRoundInfo) {
        this.uid = uid;
        roundInfo = newRoundInfo;
    }
}
