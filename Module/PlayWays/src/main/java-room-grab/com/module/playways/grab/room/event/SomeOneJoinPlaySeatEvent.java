package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabPlayerInfoModel;

/**
 * 加入观众席
 */
public class SomeOneJoinPlaySeatEvent {
    GrabPlayerInfoModel mPlayerInfoModel;

    public SomeOneJoinPlaySeatEvent(GrabPlayerInfoModel playerInfoModel) {
        this.mPlayerInfoModel = playerInfoModel;
    }
}
