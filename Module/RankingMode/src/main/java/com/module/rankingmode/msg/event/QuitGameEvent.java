package com.module.rankingmode.msg.event;

public class QuitGameEvent {

    int quitUserId;
    long quitTimeMs;

    public QuitGameEvent(int quitUserId, long quitTimeMs){
        this.quitUserId = quitUserId;
        this.quitTimeMs = quitTimeMs;
    }
}
