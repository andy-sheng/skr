package com.module.playways.grab.room.event;

import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;

public class SomeOneOnlineChangeEvent {
    public PlayerInfoModel playerInfoModel;

    public SomeOneOnlineChangeEvent(PlayerInfoModel playerInfoModel) {
        this.playerInfoModel = playerInfoModel;
    }
}
