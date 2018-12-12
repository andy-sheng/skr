package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.prepare.model.JsonGameInfo;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.JoinInfo;

import java.util.List;

public class JoinNoticeEvent {
    public BasePushInfo info;
    public JsonGameInfo jsonGameInfo;

    public JoinNoticeEvent(BasePushInfo info, JsonGameInfo jsonGameInfo) {
        this.info = info;
        this.jsonGameInfo = jsonGameInfo;
    }
}
