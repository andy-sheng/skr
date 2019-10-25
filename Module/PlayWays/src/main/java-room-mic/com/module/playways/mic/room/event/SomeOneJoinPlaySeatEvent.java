package com.module.playways.mic.room.event;

import com.module.playways.mic.room.model.MicPlayerInfoModel;

/**
 * 加入观众席
 */
public class SomeOneJoinPlaySeatEvent {
    MicPlayerInfoModel mPlayerInfoModel;

    public SomeOneJoinPlaySeatEvent(MicPlayerInfoModel playerInfoModel) {
        this.mPlayerInfoModel = playerInfoModel;
    }
}
