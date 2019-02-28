package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.rank.prepare.model.BaseRoundInfoModel;

import java.util.List;

public class GrabWaitSeatUpdateEvent {
    public List<GrabPlayerInfoModel> list;

    public GrabWaitSeatUpdateEvent(List<GrabPlayerInfoModel> list) {
        this.list = list;
    }
}
