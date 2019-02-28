package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.room.model.RankGameConfigModel;
import com.module.playways.rank.room.model.RankPlayerInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Room.JoinActionMsg;
import com.zq.live.proto.Room.QJoinActionMsg;

import java.util.ArrayList;
import java.util.List;

public class QJoinActionEvent {
    public BasePushInfo info;
    public int gameId;
    public long gameCreateMs;

    public QJoinActionEvent(BasePushInfo info, QJoinActionMsg msg) {
        this.info = info;
        this.gameId = msg.getGameID();
        this.gameCreateMs = msg.getCreateTimeMs();
    }

    @Override
    public String toString() {
        return "JoinActionEvent{" +
                "info=" + info +
                ", gameId=" + gameId +
                ", gameCreateMs=" + gameCreateMs +
                '}';
    }
}
