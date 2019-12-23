package com.module.playways.party.room.event;

import com.module.playways.party.match.model.JoinPartyRoomRspModel;

public class PartyChangeRoomEvent {
    public JoinPartyRoomRspModel mJoinGrabRoomRspModel;

    public PartyChangeRoomEvent(JoinPartyRoomRspModel rsp) {
        this.mJoinGrabRoomRspModel = rsp;
    }
}
