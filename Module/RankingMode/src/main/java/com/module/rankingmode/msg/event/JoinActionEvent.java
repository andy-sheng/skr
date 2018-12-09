package com.module.rankingmode.msg.event;

public class JoinActionEvent {
    int gameId;
    long gameCreateMs;

    public JoinActionEvent(int gameId, long gameCreateMs) {
        this.gameId = gameId;
        this.gameCreateMs = gameCreateMs;
    }
}
