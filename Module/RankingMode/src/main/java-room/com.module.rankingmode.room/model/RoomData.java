package com.module.rankingmode.room.model;

import com.module.rankingmode.prepare.model.JsonGameReadyInfo;

public class RoomData {
    int gameId;
    long createTs;
    private JsonGameReadyInfo mGameReadyInfo;

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public long getCreateTs() {
        return createTs;
    }

    public void setCreateTs(long createTs) {
        this.createTs = createTs;
    }

    public void setGameReadyInfo(JsonGameReadyInfo gameReadyInfo) {
        mGameReadyInfo = gameReadyInfo;
    }

    public JsonGameReadyInfo getGameReadyInfo() {
        return mGameReadyInfo;
    }
}
