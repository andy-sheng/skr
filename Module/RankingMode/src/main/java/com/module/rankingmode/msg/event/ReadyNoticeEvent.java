package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.zq.live.proto.Room.GameInfo;
import com.zq.live.proto.Room.ReadyInfo;
import com.zq.live.proto.Room.RoundInfo;

import java.util.List;

public class ReadyNoticeEvent {
    public BasePushInfo info;

    public List<ReadyInfo> readyInfos;//准备信息
    public List<RoundInfo> roundInfos;//轮次信息
    public GameInfo gameInfo;
    public int hasReadyedUserCnt;
    public boolean isGameStart;

    public ReadyNoticeEvent(BasePushInfo info, List<ReadyInfo> readyInfos, List<RoundInfo> roundInfos,
                            GameInfo gameInfo, int hasReadyedUserCnt, boolean isGameStart) {
        this.info = info;
        this.readyInfos = readyInfos;
        this.roundInfos = roundInfos;
        this.gameInfo = gameInfo;
        this.hasReadyedUserCnt = hasReadyedUserCnt;
        this.isGameStart = isGameStart;
    }
}
