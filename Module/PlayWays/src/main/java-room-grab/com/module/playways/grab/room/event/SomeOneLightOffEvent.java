package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.BaseRoundInfoModel;

public class SomeOneLightOffEvent {
    public BaseRoundInfoModel roundInfo;
    public int uid;

    public SomeOneLightOffEvent(int uid, BaseRoundInfoModel newRoundInfo) {
        this.uid = uid;
        roundInfo = newRoundInfo;
    }
}
