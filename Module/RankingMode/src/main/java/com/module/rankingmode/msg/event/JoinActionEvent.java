package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;

public class JoinActionEvent {
    public BasePushInfo info;
    public int gameId;
    public long gameCreateMs;

    public JoinActionEvent(BasePushInfo info, int gameId, long gameCreateMs) {
        this.info = info;
        this.gameId = gameId;
        this.gameCreateMs = gameCreateMs;
    }
}
