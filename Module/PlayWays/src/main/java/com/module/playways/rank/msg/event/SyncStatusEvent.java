package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;

import java.util.List;

public class SyncStatusEvent {

    public BasePushInfo info;

    public long syncStatusTimes;  //状态同步时的毫秒时间戳
    public long gameOverTimeMs;  //游戏结束时间

    public List<OnlineInfoModel> onlineInfos; //在线状态

    public RoundInfoModel currentInfo; //当前轮次信息
    public RoundInfoModel nextInfo; //下个轮次信息

    public SyncStatusEvent(BasePushInfo info, long syncStatusTimes, long gameOverTimeMs,
                           List<OnlineInfoModel> onlineInfos, RoundInfoModel currentInfo, RoundInfoModel nextInfo) {
        this.info = info;
        this.syncStatusTimes = syncStatusTimes;
        this.gameOverTimeMs = gameOverTimeMs;
        this.onlineInfos = onlineInfos;
        this.currentInfo = currentInfo;
        this.nextInfo = nextInfo;
    }

}
