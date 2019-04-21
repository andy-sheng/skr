package com.module.playways.grab.room.model;

import java.io.Serializable;
import java.util.HashSet;

public class ChorusRoundInfoModel implements Serializable {
    int userID;
    boolean hasGiveUp;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public boolean isHasGiveUp() {
        return hasGiveUp;
    }

    public void setHasGiveUp(boolean hasGiveUp) {
        this.hasGiveUp = hasGiveUp;
    }

}
