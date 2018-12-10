package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;

public class SyncStatusEvent {

    BasePushInfo info;

    public SyncStatusEvent(BasePushInfo info){
        this.info = info;
    }

}
