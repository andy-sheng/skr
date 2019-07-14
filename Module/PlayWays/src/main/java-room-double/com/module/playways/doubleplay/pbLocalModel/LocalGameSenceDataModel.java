package com.module.playways.doubleplay.pbLocalModel;

import com.zq.live.proto.CombineRoom.EGameStage;
import com.zq.live.proto.CombineRoom.SceneGameSyncStatusMsg;

import java.io.Serializable;

public class LocalGameSenceDataModel implements Serializable {
    private int gameStage;

    private int panelSeq;

    private int itemID;

    public int getGameStage() {
        return gameStage;
    }

    public int getPanelSeq() {
        return panelSeq;
    }

    public int getItemID() {
        return itemID;
    }

    public void setGameStage(int gameStage) {
        this.gameStage = gameStage;
    }

    public void setPanelSeq(int panelSeq) {
        this.panelSeq = panelSeq;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public LocalGameSenceDataModel() {
    }

    public LocalGameSenceDataModel(SceneGameSyncStatusMsg sceneGameSyncStatusMsg) {
        gameStage = sceneGameSyncStatusMsg.getGameStage().getValue();
        if (sceneGameSyncStatusMsg.hasPanelSeq()) {
            panelSeq = sceneGameSyncStatusMsg.getPanelSeq();
        }

        if (sceneGameSyncStatusMsg.hasItemID()) {
            itemID = sceneGameSyncStatusMsg.getItemID();
        }
    }

    public LocalGameSenceDataModel(int panelSeq) {
        gameStage = EGameStage.GS_ChoicGameItem.getValue();
        this.panelSeq = panelSeq;
    }
}
