package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.BaseRoundInfoModel;

public class GrabSomeOneLightBurstEvent {
    public BaseRoundInfoModel roundInfo;
    public int uid;

    public GrabSomeOneLightBurstEvent(int uid, BaseRoundInfoModel newRoundInfo) {
        this.uid = uid;
        this.roundInfo = newRoundInfo;
    }

    public BaseRoundInfoModel getRoundInfo() {
        return roundInfo;
    }
}
