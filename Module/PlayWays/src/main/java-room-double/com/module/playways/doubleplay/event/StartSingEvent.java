package com.module.playways.doubleplay.event;

import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic;

//从无到有的歌曲
public class StartSingEvent {
    LocalCombineRoomMusic mMusic;
    String nextDec;
    boolean hasNext = false;

    public StartSingEvent(LocalCombineRoomMusic music, String nextDec, boolean hasNext) {
        this.mMusic = music;
        this.nextDec = nextDec;
        this.hasNext = hasNext;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public LocalCombineRoomMusic getMusic() {
        return mMusic;
    }

    public String getNextDec() {
        return nextDec;
    }
}
