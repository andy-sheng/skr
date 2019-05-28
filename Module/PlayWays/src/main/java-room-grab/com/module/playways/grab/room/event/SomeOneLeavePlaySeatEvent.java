package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabPlayerInfoModel;

/**
 * 离开观众席
 */
public class SomeOneLeavePlaySeatEvent {
    public GrabPlayerInfoModel mPlayerInfoModel;

    public SomeOneLeavePlaySeatEvent(GrabPlayerInfoModel playerInfoModel) {
        this.mPlayerInfoModel = playerInfoModel;
    }
}
