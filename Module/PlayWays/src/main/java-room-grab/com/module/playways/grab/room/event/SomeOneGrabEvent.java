package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.BaseRoundInfoModel;

public class SomeOneGrabEvent {
    public BaseRoundInfoModel roundInfo;
    public int uid;

    public SomeOneGrabEvent(int uid, BaseRoundInfoModel newRoundInfo) {
        this.uid = uid;
        roundInfo = newRoundInfo;
    }
}
