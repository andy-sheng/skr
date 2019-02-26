package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.prepare.model.RankRoundInfoModel;
import com.zq.live.proto.Room.OnlineInfo;
import com.zq.live.proto.Room.SyncStatusMsg;

import java.util.ArrayList;
import java.util.List;

public class SyncStatusEvent {

    public BasePushInfo info;

    public long syncStatusTimes;  //状态同步时的毫秒时间戳
    public long gameOverTimeMs;  //游戏结束时间

    public List<OnlineInfoModel> onlineInfos; //在线状态

    public RankRoundInfoModel currentInfo; //当前轮次信息
    public RankRoundInfoModel nextInfo; //下个轮次信息

    public SyncStatusEvent(BasePushInfo info, SyncStatusMsg syncStatusMsg) {
        // TODO: 2019/2/21 这里需要把爆灯灭灯解析出来, 等PB更新
        List<OnlineInfoModel> onLineInfos = new ArrayList<>();
        for (OnlineInfo onlineInfo : syncStatusMsg.getOnlineInfoList()) {
            OnlineInfoModel jsonOnLineInfo = new OnlineInfoModel();
            jsonOnLineInfo.parse(onlineInfo);
            onLineInfos.add(jsonOnLineInfo);
        }

        this.info = info;
        this.syncStatusTimes = syncStatusMsg.getSyncStatusTimeMs();
        this.gameOverTimeMs = syncStatusMsg.getGameOverTimeMs();
        this.onlineInfos = onLineInfos;
        this.currentInfo = RankRoundInfoModel.parseFromRoundInfo(syncStatusMsg.getCurrentRound());
        this.nextInfo = RankRoundInfoModel.parseFromRoundInfo(syncStatusMsg.getNextRound());
    }
}
