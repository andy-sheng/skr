package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.prepare.model.GameReadyModel;

public class ReadyNoticeEvent {
    public BasePushInfo info;
    public GameReadyModel jsonGameReadyInfo;

    public ReadyNoticeEvent(BasePushInfo info, GameReadyModel jsonGameReadyInfo) {
        this.info = info;
        this.jsonGameReadyInfo = jsonGameReadyInfo;
    }
}
