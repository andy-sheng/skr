package com.module.playways.doubleplay.pbLocalModel;

import com.zq.live.proto.CombineRoom.SceneSingSyncStatusMsg;

import java.io.Serializable;

public class LocalSingSenceDataModel implements Serializable {
    LocalCombineRoomMusic currentMusic; //当前歌曲
    String nextMusicDesc; //下首歌曲
    boolean hasNextMusic; //是否有下首歌曲

    public void setCurrentMusic(LocalCombineRoomMusic currentMusic) {
        this.currentMusic = currentMusic;
    }

    public void setNextMusicDesc(String nextMusicDesc) {
        this.nextMusicDesc = nextMusicDesc;
    }

    public void setHasNextMusic(boolean hasNextMusic) {
        this.hasNextMusic = hasNextMusic;
    }

    public LocalCombineRoomMusic getCurrentMusic() {
        return currentMusic;
    }

    public String getNextMusicDesc() {
        return nextMusicDesc;
    }

    public boolean isHasNextMusic() {
        return hasNextMusic;
    }

    public LocalSingSenceDataModel() {

    }

    public LocalSingSenceDataModel(SceneSingSyncStatusMsg sceneSingSyncStatusMsg) {
        if (sceneSingSyncStatusMsg.hasCurrentMusic()) {
            currentMusic = new LocalCombineRoomMusic(sceneSingSyncStatusMsg.getCurrentMusic());
        }

        if (sceneSingSyncStatusMsg.hasNextMusicDesc()) {
            nextMusicDesc = sceneSingSyncStatusMsg.getNextMusicDesc();
        }

        if (sceneSingSyncStatusMsg.hasHasNextMusic()) {
            hasNextMusic = sceneSingSyncStatusMsg.getHasNextMusic();
        }
    }

    @Override
    public String toString() {
        return "LocalSingSenceDataModel{" +
                "currentMusic=" + currentMusic +
                ", nextMusicDesc='" + nextMusicDesc + '\'' +
                ", hasNextMusic=" + hasNextMusic +
                '}';
    }
}
