package com.module.playways.event;

import com.module.playways.rank.prepare.model.JoinGrabRoomRspModel;

public class GrabChangeRoomEvent {
    public JoinGrabRoomRspModel mJoinGrabRoomRspModel;

    public GrabChangeRoomEvent(JoinGrabRoomRspModel grabCurGameStateModel) {
        this.mJoinGrabRoomRspModel = grabCurGameStateModel;
    }
}
