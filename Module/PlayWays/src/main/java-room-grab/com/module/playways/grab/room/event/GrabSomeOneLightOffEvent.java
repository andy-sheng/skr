package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.BaseRoundInfoModel;

public class GrabSomeOneLightOffEvent {
    public BaseRoundInfoModel roundInfo;
    public int uid;
    public GrabSomeOneLightOffEvent(int uid, BaseRoundInfoModel newRoundInfo) {
        this.uid = uid;
        this.roundInfo = newRoundInfo;
    }

    public BaseRoundInfoModel getRoundInfo() {
        return roundInfo;
    }
}
