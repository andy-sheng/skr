package com.common.core.userinfo.model;

import java.io.Serializable;

public class OnlineModel implements Serializable {
    long onlineTime;
    int userID;

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
}
