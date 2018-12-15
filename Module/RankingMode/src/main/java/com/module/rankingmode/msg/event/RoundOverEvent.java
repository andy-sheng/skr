package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.prepare.model.RoundInfoModel;

public class RoundOverEvent {
    public BasePushInfo info;

    public long roundOverTimeMs;  //本轮次结束的毫秒时间戳
    public RoundInfoModel currenRound;  //当前轮次信息
    public RoundInfoModel nextRound;  //下个轮次信息

    public RoundOverEvent(BasePushInfo info, long roundOverTimeMs, RoundInfoModel currenRound, RoundInfoModel nextRound) {
        this.info = info;
        this.roundOverTimeMs = roundOverTimeMs;
        this.currenRound = currenRound;
        this.nextRound = nextRound;
    }
}
