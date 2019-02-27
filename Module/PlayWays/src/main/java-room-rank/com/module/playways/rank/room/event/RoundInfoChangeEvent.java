package com.module.playways.rank.room.event;

import com.module.playways.rank.prepare.model.BaseRoundInfoModel;
import com.module.playways.rank.prepare.model.RankRoundInfoModel;

public class RoundInfoChangeEvent {
    // 上一轮是谁在唱
    RankRoundInfoModel lastRoundInfoModel;
    public boolean myturn;

    public RoundInfoChangeEvent(boolean myturn,RankRoundInfoModel lastRoundInfoModel) {
        this.myturn = myturn;
        this.lastRoundInfoModel = lastRoundInfoModel;
    }

    public BaseRoundInfoModel getLastRoundInfoModel() {
        return lastRoundInfoModel;
    }
}
