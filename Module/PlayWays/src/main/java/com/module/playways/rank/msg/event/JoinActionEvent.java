package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.prepare.model.GameConfigModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.song.model.SongModel;

import java.util.List;

public class JoinActionEvent {
    public BasePushInfo info;
    public int gameId;
    public long gameCreateMs;
    public List<PlayerInfoModel> playerInfoList;
    public List<SongModel> songModelList;
    GameConfigModel gameConfigModel;

    public JoinActionEvent(BasePushInfo info, int gameId, long gameCreateMs, List<PlayerInfoModel> playerInfoList, List<SongModel> songModelList, GameConfigModel gameConfigModel) {
        this.info = info;
        this.gameId = gameId;
        this.gameCreateMs = gameCreateMs;
        this.playerInfoList = playerInfoList;
        this.songModelList = songModelList;
        this.gameConfigModel = gameConfigModel;
    }
}
