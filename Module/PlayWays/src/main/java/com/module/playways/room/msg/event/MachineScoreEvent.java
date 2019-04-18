package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.MachineScore;

public class MachineScoreEvent {

    public BasePushInfo mBasePushInfo;
    public int userId;
    public int lineNo;
    public int score;
    public int totalScore;
    public int lineNum;


    public MachineScoreEvent(BasePushInfo mBasePushInfo, MachineScore machineScore) {
        this.mBasePushInfo = mBasePushInfo;
        this.userId = machineScore.getUserID();
        this.lineNo = machineScore.getNo();
        this.score = machineScore.getScore();
        this.totalScore = machineScore.getScore();
        this.lineNum = machineScore.getLineNum();
    }
}
