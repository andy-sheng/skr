package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.prepare.model.JsonRoundInfo;

public class RoundOverEvent {
    public BasePushInfo info;

    public long roundOverTimeMs;
    public JsonRoundInfo currenRound;
    public JsonRoundInfo nextRound;

    public RoundOverEvent(BasePushInfo info, long roundOverTimeMs, JsonRoundInfo currenRound, JsonRoundInfo nextRound) {
        this.info = info;
        this.roundOverTimeMs = roundOverTimeMs;
        this.currenRound = currenRound;
        this.nextRound = nextRound;
    }
}
