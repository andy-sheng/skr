package com.module.playways.rank.msg.event;

import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.QGameBeginMsg;
import com.zq.live.proto.Room.QKickUserRequestMsg;

import java.util.List;

public class QGameBeginEvent {

    public BasePushInfo info;
    public int roomID;
    public GrabRoundInfoModel mInfoModel;

    public QGameBeginEvent(BasePushInfo info, QGameBeginMsg event) {
        this.info = info;
        this.roomID = event.getRoomID();
        this.mInfoModel = GrabRoundInfoModel.parseFromRoundInfo(event.getCurrentRound());
    }

    @Override
    public String toString() {
        return "QGameBeginEvent{" +
                ", roomID=" + roomID +
                ", mInfoModel=" + mInfoModel +
                '}';
    }
}
