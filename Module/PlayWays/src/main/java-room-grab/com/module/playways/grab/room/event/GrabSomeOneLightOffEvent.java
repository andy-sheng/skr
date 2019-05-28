package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;

public class GrabSomeOneLightOffEvent {
    public GrabRoundInfoModel roundInfo;
    public int uid;
    public GrabSomeOneLightOffEvent(int uid, GrabRoundInfoModel newRoundInfo) {
        this.uid = uid;
        this.roundInfo = newRoundInfo;
    }

    public int getUid() {
        return uid;
    }

    public GrabRoundInfoModel getRoundInfo() {
        return roundInfo;
    }

    @Override
    public String toString() {
        return "GrabSomeOneLightOffEvent{" +
                "uid=" + uid +
                '}';
    }
}
