package com.module.playways.room.room.event;

import com.module.playways.room.prepare.model.BaseRoundInfoModel;

public class PkMyLightOffSuccessEvent {
    public BaseRoundInfoModel roundInfo;

    public PkMyLightOffSuccessEvent(BaseRoundInfoModel newRoundInfo) {
        roundInfo = newRoundInfo;
    }

}
