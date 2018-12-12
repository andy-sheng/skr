package com.module.rankingmode.prepare.model;

import java.util.List;

public class GameReadyInfo {
    /**
     * readyInfo : [{"userID":1,"readySeq":1,"readyTimeMs":1544583392608},{"userID":2,"readySeq":2,"readyTimeMs":1544583397300}]
     * HasReadyedUserCnt : 2
     * isGameStart : false
     * roundInfo : null
     * gameStartInfo : null
     */

    private int HasReadyedUserCnt;
    private boolean isGameStart;
    private Object roundInfo;
    private Object gameStartInfo;
    private List<ReadyInfo> readyInfo;

    public int getHasReadyedUserCnt() {
        return HasReadyedUserCnt;
    }

    public void setHasReadyedUserCnt(int HasReadyedUserCnt) {
        this.HasReadyedUserCnt = HasReadyedUserCnt;
    }

    public boolean isIsGameStart() {
        return isGameStart;
    }

    public void setIsGameStart(boolean isGameStart) {
        this.isGameStart = isGameStart;
    }

    public Object getRoundInfo() {
        return roundInfo;
    }

    public void setRoundInfo(Object roundInfo) {
        this.roundInfo = roundInfo;
    }

    public Object getGameStartInfo() {
        return gameStartInfo;
    }

    public void setGameStartInfo(Object gameStartInfo) {
        this.gameStartInfo = gameStartInfo;
    }

    public List<ReadyInfo> getReadyInfo() {
        return readyInfo;
    }

    public void setReadyInfo(List<ReadyInfo> readyInfo) {
        this.readyInfo = readyInfo;
    }
}
