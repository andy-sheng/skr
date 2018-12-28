package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.prepare.model.GameReadyModel;

public class ReadyNoticeEvent {
    public BasePushInfo info;
    public GameReadyModel jsonGameReadyInfo;

    public ReadyNoticeEvent(BasePushInfo info, GameReadyModel jsonGameReadyInfo) {
        this.info = info;
        this.jsonGameReadyInfo = jsonGameReadyInfo;
    }
}
