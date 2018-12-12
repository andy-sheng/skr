package com.module.rankingmode.prepare.model;

import java.io.Serializable;

public class ReadyInfo implements Serializable {
    /**
     * userID : 1
     * readySeq : 1
     * readyTimeMs : 1544583392608
     */

    private int userID;
    private int readySeq;
    private long readyTimeMs;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getReadySeq() {
        return readySeq;
    }

    public void setReadySeq(int readySeq) {
        this.readySeq = readySeq;
    }

    public long getReadyTimeMs() {
        return readyTimeMs;
    }

    public void setReadyTimeMs(long readyTimeMs) {
        this.readyTimeMs = readyTimeMs;
    }
}
