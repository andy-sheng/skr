package com.module.playways.rank.room.event;

import com.module.playways.rank.prepare.model.RoundInfoModel;

public class PkMyBurstSuccessEvent {
    public RoundInfoModel roundInfo;

    public PkMyBurstSuccessEvent(RoundInfoModel newRoundInfo) {
        roundInfo = newRoundInfo;
    }

}
