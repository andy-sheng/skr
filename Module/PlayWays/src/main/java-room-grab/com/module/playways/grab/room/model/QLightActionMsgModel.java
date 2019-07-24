package com.module.playways.grab.room.model;

import com.component.live.proto.Room.QLightActionMsg;
import java.io.Serializable;

public class QLightActionMsgModel implements Serializable {
    int userID = 1; // 用户id
    int roundSeq = 2; // 轮次顺序
    int action = 3; //灭灯or爆灯

    public QLightActionMsgModel() {

    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getRoundSeq() {
        return roundSeq;
    }

    public void setRoundSeq(int roundSeq) {
        this.roundSeq = roundSeq;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public static QLightActionMsgModel parse(QLightActionMsg qLightActionMsg) {
        QLightActionMsgModel qLightActionMsgModel = new QLightActionMsgModel();
        qLightActionMsgModel.setAction(qLightActionMsg.getAction().getValue());
        qLightActionMsgModel.setRoundSeq(qLightActionMsg.getRoundSeq());
        qLightActionMsgModel.setUserID(qLightActionMsg.getUserID());
        return qLightActionMsgModel;
    }
}
