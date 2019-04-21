package com.module.playways.grab.room.model;

import com.zq.live.proto.Room.QCHOInnerRoundInfo;

import java.io.Serializable;
import java.util.HashSet;

public class ChorusRoundInfoModel implements Serializable {
    int userID;
    boolean hasGiveUp;

    public static ChorusRoundInfoModel parse(QCHOInnerRoundInfo qchoInnerRoundInfo) {
        ChorusRoundInfoModel chorusRoundInfoModel = new ChorusRoundInfoModel();
        chorusRoundInfoModel.setUserID(qchoInnerRoundInfo.getUserID());
        chorusRoundInfoModel.setHasGiveUp(qchoInnerRoundInfo.getHasGiveUp());
        return chorusRoundInfoModel;
    }

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

    public void tryUpdateRoundInfoModel(ChorusRoundInfoModel chorusRoundInfoModel2) {

    }
}
