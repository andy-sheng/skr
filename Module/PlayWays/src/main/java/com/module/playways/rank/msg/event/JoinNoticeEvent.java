package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.prepare.model.GameInfoModel;

public class JoinNoticeEvent {
    public BasePushInfo info;
    public GameInfoModel jsonGameInfo;

    public JoinNoticeEvent(BasePushInfo info, GameInfoModel jsonGameInfo) {
        this.info = info;
        this.jsonGameInfo = jsonGameInfo;
    }
}
