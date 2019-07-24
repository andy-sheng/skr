package com.module.playways.doubleplay.pushEvent;

import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.CombineRoom.AgreeChangeSceneMsg;

public class DoubleAgreeChangeSceneEvent {
    public BasePushInfo mBasePushInfo;

    private int agreeChangeUserID;

    private int sceneType;

    boolean agree;

    String noticeMsgDesc;

    public boolean isAgree() {
        return agree;
    }

    public void setAgree(boolean agree) {
        this.agree = agree;
    }

    public String getNoticeMsgDesc() {
        return noticeMsgDesc;
    }

    public void setNoticeMsgDesc(String noticeMsgDesc) {
        this.noticeMsgDesc = noticeMsgDesc;
    }

    public int getAgreeChangeUserID() {
        return agreeChangeUserID;
    }

    public int getSceneType() {
        return sceneType;
    }

    public DoubleAgreeChangeSceneEvent(BasePushInfo basePushInfo, AgreeChangeSceneMsg agreeChangeSceneMsg) {
        mBasePushInfo = basePushInfo;
        agreeChangeUserID = agreeChangeSceneMsg.getAgreeChangeUserID();
        sceneType = agreeChangeSceneMsg.getSceneType().getValue();
        agree = agreeChangeSceneMsg.getAgree();
        noticeMsgDesc = agreeChangeSceneMsg.getNoticeMsgDesc();
    }
}
