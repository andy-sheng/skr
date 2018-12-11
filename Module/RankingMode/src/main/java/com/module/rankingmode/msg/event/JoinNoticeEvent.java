package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Common.UserInfo;

public class JoinNoticeEvent {

    public long joinTimeMs;
    public UserInfo userInfo;
    public MusicInfo musicInfo;

    public BasePushInfo info;

    public JoinNoticeEvent(BasePushInfo info, long joinTimeMs, UserInfo userInfo, MusicInfo musicInfo) {
        this.info = info;
        this.joinTimeMs = joinTimeMs;
        this.userInfo = userInfo;
        this.musicInfo = musicInfo;
    }
}
