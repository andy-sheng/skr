package com.module.rankingmode.msg.event;

public class ReadyNoticeEvent {
    long readyTimeMs;
    int readyUserID;

    public ReadyNoticeEvent(long readyTimeMs, int readyUserID) {
        this.readyTimeMs = readyTimeMs;
        this.readyUserID = readyUserID;
    }
}
