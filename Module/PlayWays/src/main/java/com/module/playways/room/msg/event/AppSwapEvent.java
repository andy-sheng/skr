package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.Room.AppSwapMsg;

public class AppSwapEvent {

    BasePushInfo info;
    int swapUserId;
    long swapTimeMs;
    boolean swapOut;
    boolean swapIn;

    public AppSwapEvent(BasePushInfo info, AppSwapMsg appSwapMsg) {
        this.info = info;
        this.swapUserId = appSwapMsg.getSwapUserID();
        this.swapTimeMs = appSwapMsg.getSwapTimsMs();
        this.swapOut = appSwapMsg.getSwapOut();
        this.swapIn = appSwapMsg.getSwapIn();
    }
}
