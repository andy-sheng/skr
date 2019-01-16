package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;

public class AccBeginEvent {

    public BasePushInfo mBasePushInfo;
    public int userId;

    public AccBeginEvent(BasePushInfo mBasePushInfo, int userId) {
        this.mBasePushInfo = mBasePushInfo;
        this.userId = userId;
    }
}
