package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.QLightActionMsgModel;
import com.module.playways.rank.prepare.model.BaseRoundInfoModel;

public class GrabQLightActionEvent {
    public BaseRoundInfoModel roundInfo;
    QLightActionMsgModel mQLightActionMsgModel;

    public GrabQLightActionEvent(QLightActionMsgModel qLightActionMsgModel, BaseRoundInfoModel newRoundInfo) {
        this.mQLightActionMsgModel = qLightActionMsgModel;
        roundInfo = newRoundInfo;
    }
}
