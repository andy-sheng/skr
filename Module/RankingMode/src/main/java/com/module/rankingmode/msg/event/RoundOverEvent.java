package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.zq.live.proto.Room.RoundInfo;

public class RoundOverEvent {
    public BasePushInfo info;

    public long roundOverTimeMs;
    public RoundInfo currenRound;
    public RoundInfo nextRound;

    public RoundOverEvent(BasePushInfo info, long roundOverTimeMs, RoundInfo currenRound, RoundInfo nextRound) {
        this.info = info;
        this.roundOverTimeMs = roundOverTimeMs;
        this.currenRound = currenRound;
        this.nextRound = nextRound;
    }
}
