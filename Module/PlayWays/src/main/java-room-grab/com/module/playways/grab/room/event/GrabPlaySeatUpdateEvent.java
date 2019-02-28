package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabPlayerInfoModel;

import java.util.List;

public class GrabPlaySeatUpdateEvent {
    public List<GrabPlayerInfoModel> list;

    public GrabPlaySeatUpdateEvent(List<GrabPlayerInfoModel> list) {
        this.list = list;
    }
}
