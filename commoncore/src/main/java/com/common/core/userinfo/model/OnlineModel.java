package com.common.core.userinfo.model;

import java.io.Serializable;

public class OnlineModel implements Serializable {
    int userID;
    boolean online = false;
    long onlineTime;
    long offlineTime;

    long recordTs;

    public long getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(long onlineTime) {
        this.onlineTime = onlineTime;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getOfflineTime() {
        return offlineTime;
    }

    public void setOfflineTime(long offlineTime) {
        this.offlineTime = offlineTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnlineModel that = (OnlineModel) o;

        return userID == that.userID;
    }

    @Override
    public int hashCode() {
        return userID;
    }

    public long getRecordTs() {
        return recordTs;
    }

    public void setRecordTs(long ts) {
        recordTs = ts;
    }
}
