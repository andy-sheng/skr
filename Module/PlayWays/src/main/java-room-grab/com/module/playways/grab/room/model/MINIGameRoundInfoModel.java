package com.module.playways.grab.room.model;

import com.component.live.proto.Room.QMINIGameInnerRoundInfo;

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

    public void tryUpdateRoundInfoModel(MINIGameRoundInfoModel roundInfo) {
        if (roundInfo.getUserID() == userID) {
            setUserID(roundInfo.getUserID());
            // TODO: 2019-05-29 无状态更新
        }
    }
}
