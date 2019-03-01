package com.module.playways.rank.room.event;

import com.module.playways.rank.room.model.RankPlayerInfoModel;

public class PkSomeOneOnlineChangeEvent {
    public RankPlayerInfoModel model;

    public PkSomeOneOnlineChangeEvent(RankPlayerInfoModel m) {
        this.model = m;
    }

}
