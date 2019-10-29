package com.module.playways.room.msg.event;

import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.room.msg.BasePushInfo;
import com.module.playways.room.room.model.RankGameConfigModel;
import com.module.playways.room.room.model.RankPlayerInfoModel;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.GrabRoom.AgoraTokenInfo;
import com.zq.live.proto.GrabRoom.JoinActionMsg;
import com.zq.live.proto.GrabRoom.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class JoinActionEvent {
    public BasePushInfo info;
    public int gameId;
    public long gameCreateMs;
    public List<RankPlayerInfoModel> playerInfoList;
    public List<SongModel> songModelList;
    public RankGameConfigModel gameConfigModel;
    public String agoraToken;

    public JoinActionEvent(BasePushInfo info, JoinActionMsg joinActionMsg) {
        List<RankPlayerInfoModel> playerInfos = new ArrayList<>();
        for (PlayerInfo player : joinActionMsg.getPlayersList()) {
            RankPlayerInfoModel playerInfo = new RankPlayerInfoModel();
            playerInfo.parse(player);
            playerInfos.add(playerInfo);
        }

        List<SongModel> songModels = new ArrayList<>();
        for (MusicInfo musicInfo : joinActionMsg.getMusicList()) {
            SongModel songModel = new SongModel();
            songModel.parse(musicInfo);
            songModels.add(songModel);
        }

        RankGameConfigModel gameConfigModel = null;
        if (joinActionMsg.getConfig() != null) {
            gameConfigModel = RankGameConfigModel.parse(joinActionMsg.getConfig());
        }

        this.info = info;
        this.gameId = joinActionMsg.getGameID();
        this.gameCreateMs = joinActionMsg.getCreateTimeMs();
        this.playerInfoList = playerInfos;
        this.songModelList = songModels;
        this.gameConfigModel = gameConfigModel;
        for (AgoraTokenInfo tokenInfo : joinActionMsg.getTokensList()) {
            if (tokenInfo.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                this.agoraToken = tokenInfo.getToken();
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "JoinActionEvent{" +
                "info=" + info +
                ", gameId=" + gameId +
                ", gameCreateMs=" + gameCreateMs +
                ", playerInfoList=" + playerInfoList +
                ", songModelList=" + songModelList +
                ", gameConfigModel=" + gameConfigModel +
                '}';
    }
}
