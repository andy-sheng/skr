package com.module.playways.rank.room.event;

import com.module.playways.rank.prepare.model.RoundInfoModel;

public class PkSomeOneBurstLightEvent {
    public RoundInfoModel roundInfo;
    public int uid;

    public PkSomeOneBurstLightEvent(int uid, RoundInfoModel newRoundInfo) {
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
