package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabPlayerInfoModel;

/**
 * 离开观众席
 */
public class SomeOneLeaveWaitSeatEvent {
    GrabPlayerInfoModel mPlayerInfoModel;

    public SomeOneLeaveWaitSeatEvent(GrabPlayerInfoModel playerInfoModel) {
        this.mPlayerInfoModel = playerInfoModel;
    }

    public GrabPlayerInfoModel getPlayerInfoModel() {
        return mPlayerInfoModel;
    }
}
