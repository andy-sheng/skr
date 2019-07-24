package com.module.playways.doubleplay.pushEvent;

import com.module.playways.doubleplay.pbLocalModel.LocalGameItemInfo;
import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.CombineRoom.StartGameMsg;

public class DoubleStartGameEvent {
    public BasePushInfo mBasePushInfo;

    LocalGameItemInfo localGameItemInfo;

    public LocalGameItemInfo getLocalGameItemInfo() {
        return localGameItemInfo;
    }

    public DoubleStartGameEvent(BasePushInfo basePushInfo, StartGameMsg startGameMsg) {
        mBasePushInfo = basePushInfo;
        if (startGameMsg.hasItem()) {
            localGameItemInfo = new LocalGameItemInfo(startGameMsg.getItem());
        }
    }
}
