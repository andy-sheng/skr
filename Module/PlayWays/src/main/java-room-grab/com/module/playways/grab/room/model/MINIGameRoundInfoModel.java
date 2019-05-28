package com.module.playways.grab.room.model;

import com.zq.live.proto.Room.QMINIGameInnerRoundInfo;

import java.io.Serializable;

public class MINIGameRoundInfoModel implements Serializable {

    int userID;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public static MINIGameRoundInfoModel parse(QMINIGameInnerRoundInfo qminiGameInnerRoundInfo) {
        MINIGameRoundInfoModel miniGameRoundInfoModel = new MINIGameRoundInfoModel();
        miniGameRoundInfoModel.setUserID(qminiGameInnerRoundInfo.getUserID());
        return miniGameRoundInfoModel;
    }
}
