package com.module.playways.rank.room.event;

import com.module.playways.rank.prepare.model.RoundInfoModel;

public class PkMyLightOffSuccessEvent {
    public RoundInfoModel roundInfo;

    public PkMyLightOffSuccessEvent(RoundInfoModel newRoundInfo) {
        roundInfo = newRoundInfo;
    }

}
