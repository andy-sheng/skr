package com.common.core.userinfo.model;

import java.io.Serializable;

public class OfflineModel implements Serializable {
    long offlineTime;
    int userID;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
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

        OfflineModel that = (OfflineModel) o;

        return userID == that.userID;
    }

    @Override
    public int hashCode() {
        return userID;
    }
}
