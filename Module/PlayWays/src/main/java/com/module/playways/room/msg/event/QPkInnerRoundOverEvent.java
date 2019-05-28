package com.module.playways.room.msg.event;

import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.QCHOGiveUpMsg;
import com.zq.live.proto.Room.QSPKInnerRoundOverMsg;

public class QPkInnerRoundOverEvent {
    public BasePushInfo info;
    public long roundOverTs;
    public GrabRoundInfoModel mRoundInfoModel;

    public QPkInnerRoundOverEvent(BasePushInfo info, QSPKInnerRoundOverMsg msg) {
        this.info = info;
        this.roundOverTs = msg.getRoundOverTimeMs();
        this.mRoundInfoModel = GrabRoundInfoModel.parseFromRoundInfo(msg.getCurrentRound());
    }

    @Override
    public String toString() {
        return "QPkInnerRoundOverEvent{" +
                "mRoundInfoModel=" + mRoundInfoModel +
                '}';
    }
}