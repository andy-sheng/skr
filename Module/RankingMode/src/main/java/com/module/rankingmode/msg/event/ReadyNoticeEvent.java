package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.prepare.model.JsonGameReadyInfo;
import com.zq.live.proto.Room.GameStartInfo;
import com.zq.live.proto.Room.ReadyInfo;
import com.zq.live.proto.Room.RoundInfo;

import java.util.List;

public class ReadyNoticeEvent {
    public BasePushInfo info;
    public JsonGameReadyInfo jsonGameReadyInfo;

    public ReadyNoticeEvent(BasePushInfo info, JsonGameReadyInfo jsonGameReadyInfo) {
        this.info = info;
        this.jsonGameReadyInfo = jsonGameReadyInfo;
    }
}
