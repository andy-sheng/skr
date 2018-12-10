package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;

public class ReadyNoticeEvent {
    long readyTimeMs;
    int readyUserID;

    BasePushInfo info;

    public ReadyNoticeEvent(BasePushInfo info, long readyTimeMs, int readyUserID) {
        this.readyTimeMs = readyTimeMs;
        this.readyUserID = readyUserID;

        this.info = info;
    }
}
