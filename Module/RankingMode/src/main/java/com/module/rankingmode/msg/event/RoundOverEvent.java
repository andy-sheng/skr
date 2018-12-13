package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.prepare.model.JsonRoundInfo;

public class RoundOverEvent {
    public BasePushInfo info;

    public long roundOverTimeMs;  //本轮次结束的毫秒时间戳
    public JsonRoundInfo currenRound;  //当前轮次信息
    public JsonRoundInfo nextRound;  //下个轮次信息

    public RoundOverEvent(BasePushInfo info, long roundOverTimeMs, JsonRoundInfo currenRound, JsonRoundInfo nextRound) {
        this.info = info;
        this.roundOverTimeMs = roundOverTimeMs;
        this.currenRound = currenRound;
        this.nextRound = nextRound;
    }
}
