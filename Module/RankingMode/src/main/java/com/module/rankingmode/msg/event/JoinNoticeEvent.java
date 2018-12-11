package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.JoinInfo;

import java.util.List;

public class JoinNoticeEvent {
    public BasePushInfo info;

    List<JoinInfo> joinInfos;
    int hasJoinedUserCnt;
    int readyClockResMs;

    public JoinNoticeEvent(BasePushInfo info, List<JoinInfo> joinInfos, int hasJoinedUserCnt, int readyClockResMs) {
        this.info = info;
        this.joinInfos = joinInfos;
        this.hasJoinedUserCnt = hasJoinedUserCnt;
        this.readyClockResMs = readyClockResMs;
    }
}
