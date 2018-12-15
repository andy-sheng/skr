package com.module.rankingmode.msg.event;


import com.module.rankingmode.msg.BasePushInfo;

public class RoundAndGameOverEvent {

    public long roundOverTimeMs;
    BasePushInfo info;

    public RoundAndGameOverEvent(BasePushInfo info, long roundOverTimeMs){
        this.info = info;
        this.roundOverTimeMs = roundOverTimeMs;
    }
}
