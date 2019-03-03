package com.module.playways.rank.room.event;

import com.module.playways.rank.prepare.model.BaseRoundInfoModel;
import com.module.playways.rank.room.model.RankRoundInfoModel;

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
