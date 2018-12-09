package com.module.rankingmode.msg.event;

import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Common.UserInfo;

public class JoinNoticeEvent {

    long joinTimeMs;
    UserInfo userInfo;
    MusicInfo musicInfo;

    public JoinNoticeEvent(long joinTimeMs, UserInfo userInfo, MusicInfo musicInfo) {
        this.joinTimeMs = joinTimeMs;
        this.userInfo = userInfo;
        this.musicInfo = musicInfo;
    }
}
