package com.module.playways.rank.room.event;

import com.module.playways.grab.room.model.NoPassingInfo;
import com.module.playways.rank.prepare.model.RoundInfoModel;

public class PkSomeOneLightOffEvent {
    public RoundInfoModel roundInfo;
    public int uid;

    public PkSomeOneLightOffEvent(int uid, RoundInfoModel newRoundInfo) {
        this.uid = uid;
        roundInfo = newRoundInfo;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        PkSomeOneLightOffEvent that = (PkSomeOneLightOffEvent) object;
        return uid == that.uid;
    }

    @Override
    public int hashCode() {
        return uid;
    }
}
