package com.module.playways.party.room.event;

import com.module.playways.party.match.model.JoinPartyRoomRspModel;

import java.io.Serializable;

import io.reactivex.annotations.Nullable;

public class PartyChangeRoomEvent {
    public JoinPartyRoomRspModel mJoinGrabRoomRspModel;
    public Serializable extra;

    public PartyChangeRoomEvent(JoinPartyRoomRspModel mJoinGrabRoomRspModel, @Nullable Serializable extra) {
        this.mJoinGrabRoomRspModel = mJoinGrabRoomRspModel;
        this.extra = extra;
    }
}
