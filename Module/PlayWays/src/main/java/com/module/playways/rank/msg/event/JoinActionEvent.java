package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.prepare.model.GameConfigModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Room.JoinActionMsg;

import java.util.ArrayList;
import java.util.List;

public class JoinActionEvent {
    public BasePushInfo info;
    public int gameId;
    public long gameCreateMs;
    public List<PlayerInfoModel> playerInfoList;
    public List<SongModel> songModelList;
    GameConfigModel gameConfigModel;

    public JoinActionEvent(BasePushInfo info, JoinActionMsg joinActionMsg) {
        List<PlayerInfoModel> playerInfos = new ArrayList<>();
        for (com.zq.live.proto.Room.PlayerInfo player : joinActionMsg.getPlayersList()) {
            PlayerInfoModel playerInfo = new PlayerInfoModel();
            playerInfo.parse(player);
            playerInfos.add(playerInfo);
        }

        List<SongModel> songModels = new ArrayList<>();
        for (MusicInfo musicInfo : joinActionMsg.getCommonMusicInfoList()) {
            SongModel songModel = new SongModel();
            songModel.parse(musicInfo);
            songModels.add(songModel);
        }

        GameConfigModel gameConfigModel = null;
        if (joinActionMsg.getConfig() != null) {
            gameConfigModel = GameConfigModel.parse(joinActionMsg.getConfig());
        }

        this.info = info;
        this.gameId = joinActionMsg.getGameID();
        this.gameCreateMs = joinActionMsg.getCreateTimeMs();
        this.playerInfoList = playerInfos;
        this.songModelList = songModels;
        this.gameConfigModel = gameConfigModel;
    }
}
