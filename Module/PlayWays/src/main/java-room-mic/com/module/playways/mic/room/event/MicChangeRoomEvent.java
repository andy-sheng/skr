package com.module.playways.mic.room.event;

import com.module.playways.mic.match.model.JoinMicRoomRspModel;
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel;

public class MicChangeRoomEvent {
    public JoinMicRoomRspModel mJoinGrabRoomRspModel;

    public MicChangeRoomEvent(JoinMicRoomRspModel rsp) {
        this.mJoinGrabRoomRspModel = rsp;
    }
}
