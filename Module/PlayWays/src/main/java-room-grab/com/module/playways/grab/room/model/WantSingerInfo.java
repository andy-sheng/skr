package com.module.playways.grab.room.model;

import com.zq.live.proto.Room.WantSingInfo;

public class WantSingerInfo {
    int userID;
    long timeMs;

    public static WantSingerInfo parse(WantSingInfo pb) {
        WantSingerInfo wantSingerInfo = new WantSingerInfo();
        wantSingerInfo.setUserID(pb.getUserID());
        wantSingerInfo.setTimeMs(pb.getTimeMs());
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