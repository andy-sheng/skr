package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;

public class ExitGameEvent {

    public static final int EXIT_GAME_BEFORE_PLAY = 1;
    public static final int EXIT_GAME_AFTER_PLAY = 2;
    public static final int EXIT_GAME_OUT_ROUND = 3;

    public BasePushInfo info;
    public int type;
    public int  exitUserID; //退出玩家ID
    public long exitTimeMs; //退出毫秒时间戳

    public ExitGameEvent(BasePushInfo info, int type, int exitUserID, long exitTimeMs){
        this.info = info;
        this.type = type;
        this.exitUserID = exitUserID;
        this.exitTimeMs = exitTimeMs;
    }

}
