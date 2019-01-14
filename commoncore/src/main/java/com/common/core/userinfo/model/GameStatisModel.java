package com.common.core.userinfo.model;

import java.io.Serializable;

// 统计用户胜率
public class GameStatisModel implements Serializable {
    /**
     * mode : 1
     * totalTimes : 1
     * winPercentage : 0
     */

    private int mode;
    private int totalTimes;
    private int winPercentage;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getTotalTimes() {
        return totalTimes;
    }

    public void setTotalTimes(int totalTimes) {
        this.totalTimes = totalTimes;
    }

    public int getWinPercentage() {
        return winPercentage;
    }

    public void setWinPercentage(int winPercentage) {
        this.winPercentage = winPercentage;
    }
}
