package com.module.playways.mic.room.model;

import com.module.playways.mic.room.event.MicChorusUserStatusChangeEvent;
import com.zq.live.proto.Room.MCHOInnerRoundInfo;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

public class ChorusRoundInfoModel implements Serializable {
    int userID;
    boolean hasGiveUp;// 不唱了
    boolean hasExit;// 离线了

    public static ChorusRoundInfoModel parse(MCHOInnerRoundInfo qchoInnerRoundInfo) {
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
            boolean sendEvent = false;
            if (!isHasGiveUp() && chorusRoundInfoModel2.isHasGiveUp()) {
                setHasGiveUp(true);
                sendEvent = true;
            }
            if (isHasExit() != chorusRoundInfoModel2.isHasExit()) {
                setHasExit(chorusRoundInfoModel2.isHasExit());
                sendEvent = true;
            }
            if (sendEvent) {
                EventBus.getDefault().post(new MicChorusUserStatusChangeEvent(this));
            }
        }
    }
    public void userExit() {
        if(!hasExit){
            setHasExit(true);
            EventBus.getDefault().post(new MicChorusUserStatusChangeEvent(this));
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
