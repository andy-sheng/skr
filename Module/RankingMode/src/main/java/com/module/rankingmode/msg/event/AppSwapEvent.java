package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;

public class AppSwapEvent {

    BasePushInfo info;
    int swapUserId ;
    long swapTimeMs ;
    boolean swapOut ;
    boolean swapIn ;

    public AppSwapEvent(BasePushInfo info, int swapUserId, long swapTimeMs, boolean swapOut, boolean swapIn ){
        this.info = info;
        this.swapUserId = swapUserId;
        this.swapTimeMs = swapTimeMs;
        this.swapIn = swapIn;
        this.swapOut = swapOut;
    }
}
