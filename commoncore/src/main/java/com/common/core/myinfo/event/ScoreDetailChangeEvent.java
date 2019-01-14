package com.common.core.myinfo.event;

public class ScoreDetailChangeEvent {
    public int level;
    public int subLevel;
    public int totalStats;
    public int selecStats;

    public ScoreDetailChangeEvent(int level, int subLevel, int totalStats, int selecStats) {
        this.level = level;
        this.subLevel = subLevel;
        this.totalStats = totalStats;
        this.selecStats = selecStats;
    }
}
