package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.ExitGameAfterPlayMsg;
import com.zq.live.proto.Room.ExitGameBeforePlayMsg;
import com.zq.live.proto.Room.ExitGameOutRoundMsg;

public class ExitGameEvent {

    public static final int EXIT_GAME_BEFORE_PLAY = 1;
    public static final int EXIT_GAME_AFTER_PLAY = 2;
    public static final int EXIT_GAME_OUT_ROUND = 3;

    public BasePushInfo info;
    public int type;
    public int exitUserID; //退出玩家ID
    public long exitTimeMs; //退出毫秒时间戳

    public ExitGameEvent(BasePushInfo basePushInfo, ExitGameBeforePlayMsg exitGameBeforePlayMsg) {
        this.info = basePushInfo;
        this.type = ExitGameEvent.EXIT_GAME_BEFORE_PLAY;
        this.exitUserID = exitGameBeforePlayMsg.getExitUserID();
        this.exitTimeMs = exitGameBeforePlayMsg.getExitTimeMs();
    }

    public ExitGameEvent(BasePushInfo basePushInfo, ExitGameAfterPlayMsg exitGameAfterPlayMsg) {
        this.info = basePushInfo;
        this.type = ExitGameEvent.EXIT_GAME_AFTER_PLAY;
        this.exitTimeMs = exitGameAfterPlayMsg.getExitTimeMs();
        this.exitUserID = exitGameAfterPlayMsg.getExitUserID();
    }

    public ExitGameEvent(BasePushInfo basePushInfo, ExitGameOutRoundMsg exitGameOutRoundMsg) {
        this.info = basePushInfo;
        this.type = ExitGameEvent.EXIT_GAME_OUT_ROUND;
        this.exitUserID = exitGameOutRoundMsg.getExitUserID();
        this.exitTimeMs = exitGameOutRoundMsg.getExitTimeMs();
    }

}
