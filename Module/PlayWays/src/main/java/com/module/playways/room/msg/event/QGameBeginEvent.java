package com.module.playways.room.msg.event;

import com.module.playways.grab.room.model.GrabConfigModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.QGameBeginMsg;

public class QGameBeginEvent {

    public BasePushInfo info;
    public int roomID;
    public GrabRoundInfoModel mInfoModel;
    public GrabConfigModel mGrabConfigModel;

    public QGameBeginEvent() {
    }

    public QGameBeginEvent(BasePushInfo info, QGameBeginMsg event) {
        this.info = info;
        this.roomID = event.getRoomID();
        this.mInfoModel = GrabRoundInfoModel.parseFromRoundInfo(event.getCurrentRound());
        this.mGrabConfigModel = GrabConfigModel.parse(event.getConfig());
    }

    @Override
    public String toString() {
        return "QGameBeginEvent{" +
                ", roomID=" + roomID +
                ", mInfoModel=" + mInfoModel +
                '}';
    }
}
