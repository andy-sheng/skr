package com.module.playways.room.room.event;

import com.module.playways.room.room.model.RankRoundInfoModel;

public class RankRoundInfoChangeEvent {
    // 上一轮是谁在唱
    RankRoundInfoModel lastRoundInfoModel;
    public boolean myturn;

    public RankRoundInfoChangeEvent(boolean myturn, RankRoundInfoModel lastRoundInfoModel) {
        this.myturn = myturn;
        this.lastRoundInfoModel = lastRoundInfoModel;
    }

    public RankRoundInfoModel getLastRoundInfoModel() {
        return lastRoundInfoModel;
    }
}
