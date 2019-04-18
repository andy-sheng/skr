package com.module.playways.room.room.event;

import com.module.playways.room.room.model.RankPlayerInfoModel;

public class PkSomeOneOnlineChangeEvent {
    public RankPlayerInfoModel model;

    public PkSomeOneOnlineChangeEvent(RankPlayerInfoModel m) {
        this.model = m;
    }

}
