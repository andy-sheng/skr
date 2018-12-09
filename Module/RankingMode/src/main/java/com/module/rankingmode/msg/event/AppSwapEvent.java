package com.module.rankingmode.msg.event;

public class AppSwapEvent {

    int swapUserId ;
    long swapTimeMs ;
    boolean swapOut ;
    boolean swapIn ;

    public AppSwapEvent(int swapUserId,long swapTimeMs,boolean swapOut,boolean swapIn ){
        this.swapUserId = swapUserId;
        this.swapTimeMs = swapTimeMs;
        this.swapIn = swapIn;
        this.swapOut = swapOut;
    }
}
