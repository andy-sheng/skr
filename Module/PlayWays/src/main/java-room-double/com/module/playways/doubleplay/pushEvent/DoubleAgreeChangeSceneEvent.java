package com.module.playways.doubleplay.pushEvent;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.AgreeChangeSceneMsg;

public class DoubleAgreeChangeSceneEvent {
    public BasePushInfo mBasePushInfo;

    private int agreeChangeUserID;

    private int sceneType;

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
    }
}
