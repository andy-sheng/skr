package com.module.playways.rank.room.event;

import com.module.playways.rank.prepare.model.BaseRoundInfoModel;

public class PkMyLightOffSuccessEvent {
    public BaseRoundInfoModel roundInfo;

    public PkMyLightOffSuccessEvent(BaseRoundInfoModel newRoundInfo) {
        roundInfo = newRoundInfo;
    }

}
