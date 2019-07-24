package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.Room.QJoinActionMsg;

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
