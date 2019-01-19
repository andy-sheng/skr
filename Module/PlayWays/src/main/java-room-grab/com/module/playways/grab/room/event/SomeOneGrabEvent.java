package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.RoundInfoModel;

public class SomeOneGrabEvent {
    public RoundInfoModel roundInfo;
    public int uid;

    public SomeOneGrabEvent(int uid, RoundInfoModel newRoundInfo) {
        this.uid = uid;
        roundInfo = newRoundInfo;
    }
}
