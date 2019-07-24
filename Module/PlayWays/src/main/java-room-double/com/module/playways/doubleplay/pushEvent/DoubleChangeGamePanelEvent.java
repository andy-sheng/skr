package com.module.playways.doubleplay.pushEvent;

import com.module.playways.doubleplay.pbLocalModel.LocalGamePanelInfo;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.ChangeGamePanelMsg;

public class DoubleChangeGamePanelEvent {
    public BasePushInfo mBasePushInfo;

    LocalGamePanelInfo localGamePanelInfo;

    public LocalGamePanelInfo getLocalGamePanelInfo() {
        return localGamePanelInfo;
    }

    public DoubleChangeGamePanelEvent(BasePushInfo basePushInfo, ChangeGamePanelMsg changeGamePanelMsg) {
        mBasePushInfo = basePushInfo;
        if (changeGamePanelMsg.hasNextPanel()) {
            localGamePanelInfo = LocalGamePanelInfo.pb2LocalModel(changeGamePanelMsg.getNextPanel());
        }
    }
}
