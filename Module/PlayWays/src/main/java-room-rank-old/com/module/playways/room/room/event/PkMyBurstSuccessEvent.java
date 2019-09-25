package com.module.playways.room.room.event;

import com.module.playways.room.prepare.model.BaseRoundInfoModel;

public class PkMyBurstSuccessEvent {
    public BaseRoundInfoModel roundInfo;

    public PkMyBurstSuccessEvent(BaseRoundInfoModel newRoundInfo) {
        roundInfo = newRoundInfo;
    }

}
