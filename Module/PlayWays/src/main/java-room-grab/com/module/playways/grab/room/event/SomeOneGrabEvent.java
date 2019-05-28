package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.WantSingerInfo;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;

public class SomeOneGrabEvent {
    public BaseRoundInfoModel roundInfo;
    public WantSingerInfo mWantSingerInfo;

    public SomeOneGrabEvent(WantSingerInfo wantSingerInfo, BaseRoundInfoModel newRoundInfo) {
        this.mWantSingerInfo = wantSingerInfo;
        roundInfo = newRoundInfo;
    }
}
