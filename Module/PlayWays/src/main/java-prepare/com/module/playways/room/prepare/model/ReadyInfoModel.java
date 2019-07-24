package com.module.playways.room.prepare.model;

import com.common.log.MyLog;
import com.component.live.proto.Room.ReadyInfo;

import java.io.Serializable;

public class ReadyInfoModel implements Serializable {
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

    public void parse(ReadyInfo readyInfo) {
        if (readyInfo == null) {
            MyLog.e("JsonReadyInfo ReadyInfo == null");
            return;
        }

        this.setUserID(readyInfo.getUserID());
        this.setReadySeq(readyInfo.getReadySeq());
        this.setReadyTimeMs(readyInfo.getReadyTimeMs());
        return;
    }

    @Override
    public String toString() {
        return "JsonReadyInfo{" +
                "userID=" + userID +
                ", readySeq=" + readySeq +
                ", readyTimeMs=" + readyTimeMs +
                '}';
    }
}
