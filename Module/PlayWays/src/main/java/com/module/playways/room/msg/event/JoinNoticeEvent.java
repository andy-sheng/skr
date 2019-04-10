package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.module.playways.room.prepare.model.GameInfoModel;
import com.zq.live.proto.Room.JoinNoticeMsg;

public class JoinNoticeEvent {
    public BasePushInfo info;
    public GameInfoModel jsonGameInfo;

    public JoinNoticeEvent(BasePushInfo info, JoinNoticeMsg joinNoticeMsg) {
        GameInfoModel jsonGameInfo = new GameInfoModel();
        jsonGameInfo.parse(joinNoticeMsg);
        this.info = info;
        this.jsonGameInfo = jsonGameInfo;
    }
}
