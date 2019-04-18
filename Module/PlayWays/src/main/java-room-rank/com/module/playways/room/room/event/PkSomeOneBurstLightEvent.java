package com.module.playways.room.room.event;

import com.module.playways.room.prepare.model.BaseRoundInfoModel;

public class PkSomeOneBurstLightEvent {
    public BaseRoundInfoModel roundInfo;
    public int uid;

    public PkSomeOneBurstLightEvent(int uid, BaseRoundInfoModel newRoundInfo) {
        this.uid = uid;
        roundInfo = newRoundInfo;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        PkSomeOneBurstLightEvent that = (PkSomeOneBurstLightEvent) object;
        return uid == that.uid;
    }

    @Override
    public int hashCode() {
        return uid;
    }
}
