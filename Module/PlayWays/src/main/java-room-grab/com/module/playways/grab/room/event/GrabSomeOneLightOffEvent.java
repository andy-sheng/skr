package com.module.playways.grab.room.event;

import com.module.playways.room.prepare.model.BaseRoundInfoModel;

public class GrabSomeOneLightOffEvent {
    public BaseRoundInfoModel roundInfo;
    public int uid;
    public GrabSomeOneLightOffEvent(int uid, BaseRoundInfoModel newRoundInfo) {
        this.uid = uid;
        this.roundInfo = newRoundInfo;
    }

    public int getUid() {
        return uid;
    }

    public BaseRoundInfoModel getRoundInfo() {
        return roundInfo;
    }

    @Override
    public String toString() {
        return "GrabSomeOneLightOffEvent{" +
                "uid=" + uid +
                '}';
    }
}
