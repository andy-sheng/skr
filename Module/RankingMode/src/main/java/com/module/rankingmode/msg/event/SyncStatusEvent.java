package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.prepare.model.JsonRoundInfo;
import com.zq.live.proto.Room.OnlineInfo;

import java.util.List;

public class SyncStatusEvent {

    public BasePushInfo info;

    public long syncStatusTimes;  //状态同步时的毫秒时间戳
    public long gameOverTimeMs;  //游戏结束时间

    public List<OnlineInfo> onlineInfos; //在线状态

    public JsonRoundInfo currentInfo; //当前轮次信息
    public JsonRoundInfo nextInfo; //下个轮次信息

    public SyncStatusEvent(BasePushInfo info, long syncStatusTimes, long gameOverTimeMs,
                           List<OnlineInfo> onlineInfos, JsonRoundInfo currentInfo, JsonRoundInfo nextInfo) {
        this.info = info;
        this.syncStatusTimes = syncStatusTimes;
        this.gameOverTimeMs = gameOverTimeMs;
        this.onlineInfos = onlineInfos;
        this.currentInfo = currentInfo;
        this.nextInfo = nextInfo;
    }

}
