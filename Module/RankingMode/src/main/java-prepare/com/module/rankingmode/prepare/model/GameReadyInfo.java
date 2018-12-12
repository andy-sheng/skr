package com.module.rankingmode.prepare.model;

import java.io.Serializable;
import java.util.List;

public class GameReadyInfo implements Serializable {
    /**
     * readyInfo : [{"userID":7,"readySeq":1,"readyTimeMs":1544584287997},{"userID":8,"readySeq":2,"readyTimeMs":1544584290741},{"userID":9,"readySeq":3,"readyTimeMs":1544586876228}]
     * HasReadyedUserCnt : 3
     * isGameStart : true
     * roundInfo : [{"userID":7,"playbookID":1,"roundSeq":1,"singBeginMs":3000,"singEndMs":341000},{"userID":8,"playbookID":1,"roundSeq":2,"singBeginMs":344000,"singEndMs":682000},{"userID":9,"playbookID":1,"roundSeq":3,"singBeginMs":685000,"singEndMs":1023000}]
     * gameStartInfo : {"startTimeMs":1544586876239,"startPassedMs":3119}
     */

    private int HasReadyedUserCnt;
    private boolean isGameStart;
    private GameStartInfo gameStartInfo;
    private List<ReadyInfo> readyInfo;
    private List<RoundInfo> roundInfo;

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

    public GameStartInfo getGameStartInfo() {
        return gameStartInfo;
    }

    public void setGameStartInfo(GameStartInfo gameStartInfo) {
        this.gameStartInfo = gameStartInfo;
    }

    public List<ReadyInfo> getReadyInfo() {
        return readyInfo;
    }

    public void setReadyInfo(List<ReadyInfo> readyInfo) {
        this.readyInfo = readyInfo;
    }

    public List<RoundInfo> getRoundInfo() {
        return roundInfo;
    }

    public void setRoundInfo(List<RoundInfo> roundInfo) {
        this.roundInfo = roundInfo;
    }
}
