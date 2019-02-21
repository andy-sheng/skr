package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.prepare.model.GameReadyModel;
import com.zq.live.proto.Room.ReadyNoticeMsg;

public class ReadyNoticeEvent {
    public BasePushInfo info;
    public GameReadyModel gameReadyInfo;

    public ReadyNoticeEvent(BasePushInfo info, ReadyNoticeMsg readyNoticeMsg) {
        GameReadyModel jsonGameReadyInfo = new GameReadyModel();
        jsonGameReadyInfo.parse(readyNoticeMsg);
        this.info = info;
        this.gameReadyInfo = jsonGameReadyInfo;
    }

    @Override
    public String toString() {
        return "ReadyNoticeEvent{" +
                "jsonGameReadyInfo=" + gameReadyInfo +
                '}';
    }
}
