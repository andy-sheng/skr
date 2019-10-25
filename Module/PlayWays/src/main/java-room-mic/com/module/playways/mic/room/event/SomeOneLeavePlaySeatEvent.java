package com.module.playways.mic.room.event;

import com.module.playways.mic.room.model.MicPlayerInfoModel;

/**
 * 离开观众席
 */
public class SomeOneLeavePlaySeatEvent {
    public MicPlayerInfoModel mPlayerInfoModel;

    public SomeOneLeavePlaySeatEvent(MicPlayerInfoModel playerInfoModel) {
        this.mPlayerInfoModel = playerInfoModel;
    }
}
