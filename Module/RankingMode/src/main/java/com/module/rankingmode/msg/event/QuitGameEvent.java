package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;

public class QuitGameEvent {

    int quitUserId;
    long quitTimeMs;

    BasePushInfo info;

    public QuitGameEvent(BasePushInfo info, int quitUserId, long quitTimeMs) {
        this.info = info;
        this.quitUserId = quitUserId;
        this.quitTimeMs = quitTimeMs;
    }
}
