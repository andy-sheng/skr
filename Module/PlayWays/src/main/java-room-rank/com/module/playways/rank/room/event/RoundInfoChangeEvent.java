package com.module.playways.rank.room.event;

import com.module.playways.rank.prepare.model.BaseRoundInfoModel;

public class RoundInfoChangeEvent {
    // 上一轮是谁在唱
    BaseRoundInfoModel lastRoundInfoModel;
    public boolean myturn;

    public RoundInfoChangeEvent(boolean myturn,BaseRoundInfoModel lastRoundInfoModel) {
        this.myturn = myturn;
        this.lastRoundInfoModel = lastRoundInfoModel;
    }

    public BaseRoundInfoModel getLastRoundInfoModel() {
        return lastRoundInfoModel;
    }
}
