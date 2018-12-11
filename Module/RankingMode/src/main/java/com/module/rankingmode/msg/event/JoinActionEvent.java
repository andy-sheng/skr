package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Room.PlayerInfo;

import java.util.List;

public class JoinActionEvent {
    public BasePushInfo info;
    public int gameId;
    public long gameCreateMs;
    public List<PlayerInfo> playerInfoList;
    public List<MusicInfo> musicInfoList;

    public JoinActionEvent(BasePushInfo info, int gameId, long gameCreateMs, List<PlayerInfo> playerInfoList, List<MusicInfo> musicInfoList) {
        this.info = info;
        this.gameId = gameId;
        this.gameCreateMs = gameCreateMs;
        this.playerInfoList = playerInfoList;
        this.musicInfoList = musicInfoList;
    }
}
