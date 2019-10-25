package com.module.playways.mic.room.event;

import com.module.playways.mic.room.model.MicPlayerInfoModel;

import java.util.List;

public class MicPlaySeatUpdateEvent {
    public List<MicPlayerInfoModel> list;

    public MicPlaySeatUpdateEvent(List<MicPlayerInfoModel> list) {
        this.list = list;
    }
}
