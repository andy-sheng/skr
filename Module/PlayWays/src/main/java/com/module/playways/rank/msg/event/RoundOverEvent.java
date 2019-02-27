package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.room.model.RankRoundInfoModel;
import com.zq.live.proto.Room.RoundOverMsg;

public class RoundOverEvent {
    public BasePushInfo info;

    public long roundOverTimeMs;  //本轮次结束的毫秒时间戳
    public RankRoundInfoModel currenRound;  //当前轮次信息
    public RankRoundInfoModel nextRound;  //下个轮次信息
    public int exitUserID; //退出用户的ID, 无退出会则为0

    public RoundOverEvent(BasePushInfo info, RoundOverMsg roundOverMsgr) {
        this.info = info;
        this.roundOverTimeMs = roundOverMsgr.getRoundOverTimeMs();
        this.currenRound = RankRoundInfoModel.parseFromRoundInfo(roundOverMsgr.getCurrentRound());
        this.nextRound = RankRoundInfoModel.parseFromRoundInfo(roundOverMsgr.getNextRound());
        this.exitUserID = roundOverMsgr.getExitUserID();
    }
}
