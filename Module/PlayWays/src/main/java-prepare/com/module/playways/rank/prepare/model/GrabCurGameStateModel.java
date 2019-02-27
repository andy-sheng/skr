package com.module.playways.rank.prepare.model;

import java.io.Serializable;
import java.util.List;

public class GrabCurGameStateModel implements Serializable {


    /**
     * coin : 0
     * elapsedTimeMs : 0
     * gameOverTimeMs : 0
     * roomID : 0
     * syncStatusTimeMs : 0
     */

    private int coin;
    private int elapsedTimeMs;
    private int gameOverTimeMs;
    private int roomID;
    private int syncStatusTimeMs;
    private GrabRoundInfoModel curGrabRoundInfoModel;
    private GrabRoundInfoModel nextGrabRoundInfoModel;
    private List<OnlineInfoModel> onlineInfoModels;

    public GrabCurGameStateModel(){

    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public int getElapsedTimeMs() {
        return elapsedTimeMs;
    }

    public void setElapsedTimeMs(int elapsedTimeMs) {
        this.elapsedTimeMs = elapsedTimeMs;
    }

    public int getGameOverTimeMs() {
        return gameOverTimeMs;
    }

    public void setGameOverTimeMs(int gameOverTimeMs) {
        this.gameOverTimeMs = gameOverTimeMs;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public int getSyncStatusTimeMs() {
        return syncStatusTimeMs;
    }

    public void setSyncStatusTimeMs(int syncStatusTimeMs) {
        this.syncStatusTimeMs = syncStatusTimeMs;
    }

    public GrabRoundInfoModel getCurGrabRoundInfoModel() {
        return curGrabRoundInfoModel;
    }

    public void setCurGrabRoundInfoModel(GrabRoundInfoModel curGrabRoundInfoModel) {
        this.curGrabRoundInfoModel = curGrabRoundInfoModel;
    }

    public GrabRoundInfoModel getNextGrabRoundInfoModel() {
        return nextGrabRoundInfoModel;
    }

    public void setNextGrabRoundInfoModel(GrabRoundInfoModel nextGrabRoundInfoModel) {
        this.nextGrabRoundInfoModel = nextGrabRoundInfoModel;
    }

    public List<OnlineInfoModel> getOnlineInfoModels() {
        return onlineInfoModels;
    }

    public void setOnlineInfoModels(List<OnlineInfoModel> onlineInfoModels) {
        this.onlineInfoModels = onlineInfoModels;
    }
}
