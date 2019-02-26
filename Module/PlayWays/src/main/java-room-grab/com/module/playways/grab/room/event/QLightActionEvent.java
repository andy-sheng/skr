package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.QLightActionMsgModel;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.zq.live.proto.Room.QLightActionMsg;

public class QLightActionEvent {
    BasePushInfo info;
    QLightActionMsg qLightActionMsg;

    public QLightActionEvent(QLightActionMsg qLightActionMsgModel, BasePushInfo info) {
        this.qLightActionMsg = qLightActionMsgModel;
        this.info = info;
    }

    public BasePushInfo getInfo() {
        return info;
    }

    public QLightActionMsg getqLightActionMsg() {
        return qLightActionMsg;
    }
}
