package com.module.playways.doubleplay.event;

public class UpdateRoomSceneEvent {
    int preScene;
    int curScene;

    public int getPreScene() {
        return preScene;
    }

    public int getCurScene() {
        return curScene;
    }

    public UpdateRoomSceneEvent(int preScene, int curScene) {
        this.preScene = preScene;
        this.curScene = curScene;
    }
}
