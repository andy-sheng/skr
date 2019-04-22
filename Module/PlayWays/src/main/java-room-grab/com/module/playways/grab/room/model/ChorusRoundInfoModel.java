package com.module.playways.grab.room.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.event.GrabGiveUpInChorusEvent;
import com.zq.live.proto.Room.QCHOInnerRoundInfo;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.HashSet;

public class ChorusRoundInfoModel implements Serializable {
    int userID;
    boolean hasGiveUp;
    boolean hasExit;

    public static ChorusRoundInfoModel parse(QCHOInnerRoundInfo qchoInnerRoundInfo) {
        ChorusRoundInfoModel chorusRoundInfoModel = new ChorusRoundInfoModel();
        chorusRoundInfoModel.setUserID(qchoInnerRoundInfo.getUserID());
        chorusRoundInfoModel.setHasGiveUp(qchoInnerRoundInfo.getHasGiveUp());
        chorusRoundInfoModel.setHasExit(qchoInnerRoundInfo.getHasExit());
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

    public boolean isHasExit() {
        return hasExit;
    }

    public void setHasExit(boolean hasExit) {
        this.hasExit = hasExit;
    }

    public void tryUpdateRoundInfoModel(ChorusRoundInfoModel chorusRoundInfoModel2) {
        if (chorusRoundInfoModel2.getUserID() == userID) {
            if (!isHasGiveUp() && chorusRoundInfoModel2.isHasGiveUp()) {
                setHasGiveUp(true);
                EventBus.getDefault().post(new GrabGiveUpInChorusEvent(this));
            }
            if(isHasExit()!=chorusRoundInfoModel2.isHasExit()){
                setHasExit(chorusRoundInfoModel2.isHasExit());
            }
        }
    }

    @Override
    public String toString() {
        return "ChorusRoundInfoModel{" +
                "userID=" + userID +
                ", hasGiveUp=" + hasGiveUp +
                ", hasExit=" + hasExit +
                '}';
    }
}
