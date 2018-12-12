package com.module.rankingmode.prepare.model;

import java.io.Serializable;

public class GameStartInfo implements Serializable {
    /**
     * startTimeMs : 1544586876239
     * startPassedMs : 3119
     */

    private long startTimeMs;
    private int startPassedMs;

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public int getStartPassedMs() {
        return startPassedMs;
    }

    public void setStartPassedMs(int startPassedMs) {
        this.startPassedMs = startPassedMs;
    }
}