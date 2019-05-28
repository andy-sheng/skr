package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;

public class GrabSomeOneLightBurstEvent {
    public GrabRoundInfoModel roundInfo;
    public int uid;

    public GrabSomeOneLightBurstEvent(int uid, GrabRoundInfoModel newRoundInfo) {
        this.uid = uid;
        this.roundInfo = newRoundInfo;
    }

    public GrabRoundInfoModel getRoundInfo() {
        return roundInfo;
    }
}
