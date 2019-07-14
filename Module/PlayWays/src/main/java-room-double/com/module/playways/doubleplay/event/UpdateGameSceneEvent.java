package com.module.playways.doubleplay.event;

import com.module.playways.doubleplay.pbLocalModel.LocalGameSenceDataModel;

public class UpdateGameSceneEvent {
    LocalGameSenceDataModel localGameSenceDataModel;

    public LocalGameSenceDataModel getLocalGameSenceDataModel() {
        return localGameSenceDataModel;
    }

    public UpdateGameSceneEvent(LocalGameSenceDataModel localGameSenceDataModel) {
        this.localGameSenceDataModel = localGameSenceDataModel;
    }
}
