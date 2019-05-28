package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabPlayerInfoModel;

import java.util.List;

public class GrabWaitSeatUpdateEvent {
    public List<GrabPlayerInfoModel> list;

    public GrabWaitSeatUpdateEvent(List<GrabPlayerInfoModel> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "GrabWaitSeatUpdateEvent{" +
                "list=" + list +
                '}';
    }
}
