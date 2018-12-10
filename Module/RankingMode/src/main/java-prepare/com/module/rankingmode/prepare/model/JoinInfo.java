package com.module.rankingmode.prepare.model;

public class JoinInfo {

    /**
     * userID : 30
     * joinSeq : 1
     * joinTimeMs : 1544439278416
     */

    private int userID;
    private int joinSeq;
    private long joinTimeMs;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getJoinSeq() {
        return joinSeq;
    }

    public void setJoinSeq(int joinSeq) {
        this.joinSeq = joinSeq;
    }

    public long getJoinTimeMs() {
        return joinTimeMs;
    }

    public void setJoinTimeMs(long joinTimeMs) {
        this.joinTimeMs = joinTimeMs;
    }
}
