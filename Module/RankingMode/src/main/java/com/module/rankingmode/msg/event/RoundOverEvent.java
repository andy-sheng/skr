package com.module.rankingmode.msg.event;

public class RoundOverEvent {
    long roundOverTimeMs;
    int nextRoundSeq;
    int nextUserId;
    int nextMusicId;

    public RoundOverEvent(long roundOverTimeMs, int nextRoundSeq, int nextUserId, int nextMusicId) {
        this.roundOverTimeMs = roundOverTimeMs;
        this.nextRoundSeq = nextRoundSeq;
        this.nextUserId = nextUserId;
        this.nextMusicId = nextMusicId;
    }
}
