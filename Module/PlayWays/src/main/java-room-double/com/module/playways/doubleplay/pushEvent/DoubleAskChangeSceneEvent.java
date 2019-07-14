package com.module.playways.doubleplay.pushEvent;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.ReqChangeSceneMsg;

public class DoubleAskChangeSceneEvent {
    public BasePushInfo mBasePushInfo;

    private int reqChangeUserID;

    private int sceneType;

    public int getReqChangeUserID() {
        return reqChangeUserID;
    }

    public int getSceneType() {
        return sceneType;
    }

    public DoubleAskChangeSceneEvent(BasePushInfo basePushInfo, ReqChangeSceneMsg reqChangeSceneMsg) {
        mBasePushInfo = basePushInfo;
        reqChangeUserID = reqChangeSceneMsg.getReqChangeUserID();
        sceneType = reqChangeSceneMsg.getSceneType().getValue();
    }
}
