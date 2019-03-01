package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.BaseRoundInfoModel;

public class SomeOneLightBurstEvent {
    public BaseRoundInfoModel roundInfo;
    public int uid;

    public SomeOneLightBurstEvent(int uid, BaseRoundInfoModel newRoundInfo) {
        this.uid = uid;
        roundInfo = newRoundInfo;
    }

    public BaseRoundInfoModel getRoundInfo() {
        return roundInfo;
    }
}
