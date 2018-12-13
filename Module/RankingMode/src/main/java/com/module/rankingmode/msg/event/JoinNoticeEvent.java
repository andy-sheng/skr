package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.prepare.model.JsonGameInfo;

public class JoinNoticeEvent {
    public BasePushInfo info;
    public JsonGameInfo jsonGameInfo;

    public JoinNoticeEvent(BasePushInfo info, JsonGameInfo jsonGameInfo) {
        this.info = info;
        this.jsonGameInfo = jsonGameInfo;
    }
}
