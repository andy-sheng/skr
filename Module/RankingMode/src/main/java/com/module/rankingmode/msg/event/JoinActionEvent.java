package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.prepare.model.PlayerInfo;
import com.module.rankingmode.song.model.SongModel;

import java.util.List;

public class JoinActionEvent {
    public BasePushInfo info;
    public int gameId;
    public long gameCreateMs;
    public List<PlayerInfo> playerInfoList;
    public List<SongModel> songModelList;

    public JoinActionEvent(BasePushInfo info, int gameId, long gameCreateMs, List<PlayerInfo> playerInfoList, List<SongModel> songModelList) {
        this.info = info;
        this.gameId = gameId;
        this.gameCreateMs = gameCreateMs;
        this.playerInfoList = playerInfoList;
        this.songModelList = songModelList;
    }
}
