package com.module.playways.rank.room.event;

import com.module.playways.rank.prepare.model.RoundInfoModel;

public class RoundInfoChangeEvent {
    // 上一轮是谁在唱
    RoundInfoModel lastRoundInfoModel;
    public boolean myturn;

    public RoundInfoChangeEvent(boolean myturn,RoundInfoModel lastRoundInfoModel) {
        this.myturn = myturn;
        this.lastRoundInfoModel = lastRoundInfoModel;
    }

    public RoundInfoModel getLastRoundInfoModel() {
        return lastRoundInfoModel;
    }
}
