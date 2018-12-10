package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;

public class RoundOverEvent {
    long roundOverTimeMs;
    int nextRoundSeq;
    int nextUserId;
    int nextMusicId;

    BasePushInfo info;

    public RoundOverEvent(BasePushInfo info, long roundOverTimeMs, int nextRoundSeq, int nextUserId, int nextMusicId) {
        this.roundOverTimeMs = roundOverTimeMs;
        this.nextRoundSeq = nextRoundSeq;
        this.nextUserId = nextUserId;
        this.nextMusicId = nextMusicId;

        this.info = info;
    }
}
