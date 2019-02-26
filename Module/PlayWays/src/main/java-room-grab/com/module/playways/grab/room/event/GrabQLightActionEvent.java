package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.QLightActionMsgModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;

public class GrabQLightActionEvent {
    public RoundInfoModel roundInfo;
    QLightActionMsgModel mQLightActionMsgModel;

    public GrabQLightActionEvent(QLightActionMsgModel qLightActionMsgModel, RoundInfoModel newRoundInfo) {
        this.mQLightActionMsgModel = qLightActionMsgModel;
        roundInfo = newRoundInfo;
    }
}
