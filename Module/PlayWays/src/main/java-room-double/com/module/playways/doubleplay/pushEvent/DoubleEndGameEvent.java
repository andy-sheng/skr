package com.module.playways.doubleplay.pushEvent;

import com.module.playways.doubleplay.pbLocalModel.LocalGamePanelInfo;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.EndGameMsg;

public class DoubleEndGameEvent {
    public BasePushInfo mBasePushInfo;

    LocalGamePanelInfo localGamePanelInfo;

    public LocalGamePanelInfo getLocalGamePanelInfo() {
        return localGamePanelInfo;
    }

    public DoubleEndGameEvent(BasePushInfo basePushInfo, EndGameMsg endGameMsg) {
        mBasePushInfo = basePushInfo;
        if (endGameMsg.hasNextPanel()) {
            localGamePanelInfo = LocalGamePanelInfo.pb2LocalModel(endGameMsg.getNextPanel());
        }
    }
}
