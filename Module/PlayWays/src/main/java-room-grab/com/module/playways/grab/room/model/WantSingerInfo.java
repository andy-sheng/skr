package com.module.playways.grab.room.model;

import com.zq.live.proto.Room.WantSingInfo;

import java.io.Serializable;

public class WantSingerInfo implements Serializable {
    int userID;
    long timeMs;
    int wantSingType;
    public static WantSingerInfo parse(WantSingInfo pb) {
        WantSingerInfo wantSingerInfo = new WantSingerInfo();
        wantSingerInfo.setUserID(pb.getUserID());
        wantSingerInfo.setTimeMs(pb.getTimeMs());
//        wantSingerInfo.setWantSingType(pb.get);
        return wantSingerInfo;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(long timeMs) {
        this.timeMs = timeMs;
    }

    public int getWantSingType() {
        return wantSingType;
    }

    public void setWantSingType(int wantSingType) {
        this.wantSingType = wantSingType;
    }

    @Override
    public String toString() {
        return "WantSinger{" +
                "userID=" + userID +
                ", timeMs=" + timeMs +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        WantSingerInfo that = (WantSingerInfo) object;
        return userID == that.userID;
    }

    @Override
    public int hashCode() {
        return userID;
    }
}