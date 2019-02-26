package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.PlayerInfoModel;

public class SomeOneOnlineChangeEvent {
    public PlayerInfoModel playerInfoModel;

    public SomeOneOnlineChangeEvent(PlayerInfoModel playerInfoModel) {
        this.playerInfoModel = playerInfoModel;
    }
}
