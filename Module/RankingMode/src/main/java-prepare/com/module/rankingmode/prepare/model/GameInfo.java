package com.module.rankingmode.prepare.model;

import java.io.Serializable;
import java.util.List;

public class GameInfo implements Serializable {
    /**
     * joinInfo : [{"userID":30,"joinSeq":1,"joinTimeMs":1544439278416},{"userID":20,"joinSeq":2,"joinTimeMs":1544439286547},{"userID":10,"joinSeq":3,"joinTimeMs":1544441838343}]
     * hasJoinedUserCnt : 3
     * readyClockResMs : -7986350
     */

    private int hasJoinedUserCnt;
    private int readyClockResMs;
    private List<JoinInfo> joinInfo;

    public int getHasJoinedUserCnt() {
        return hasJoinedUserCnt;
    }

    public void setHasJoinedUserCnt(int hasJoinedUserCnt) {
        this.hasJoinedUserCnt = hasJoinedUserCnt;
    }

    public int getReadyClockResMs() {
        return readyClockResMs;
    }

    public void setReadyClockResMs(int readyClockResMs) {
        this.readyClockResMs = readyClockResMs;
    }

    public List<JoinInfo> getJoinInfo() {
        return joinInfo;
    }

    public void setJoinInfo(List<JoinInfo> joinInfo) {
        this.joinInfo = joinInfo;
    }
}
