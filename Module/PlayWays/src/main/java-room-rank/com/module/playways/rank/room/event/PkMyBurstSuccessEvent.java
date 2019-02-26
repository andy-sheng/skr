package com.module.playways.rank.room.event;

import com.module.playways.rank.prepare.model.BaseRoundInfoModel;

public class PkMyBurstSuccessEvent {
    public BaseRoundInfoModel roundInfo;

    public PkMyBurstSuccessEvent(BaseRoundInfoModel newRoundInfo) {
        roundInfo = newRoundInfo;
    }

}
