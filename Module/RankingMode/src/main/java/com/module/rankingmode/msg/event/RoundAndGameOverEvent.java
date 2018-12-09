package com.module.rankingmode.msg.event;


public class RoundAndGameOverEvent {

    long roundOverTimeMs;

    public RoundAndGameOverEvent(long roundOverTimeMs){
        this.roundOverTimeMs = roundOverTimeMs;
    }
}
