package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;

public class AccBeginEvent {

    public BasePushInfo mBasePushInfo;
    public int userId;

    public AccBeginEvent(BasePushInfo mBasePushInfo, int userId) {
        this.mBasePushInfo = mBasePushInfo;
        this.userId = userId;
    }
}
