package com.module.playways.doubleplay.event;

import com.module.playways.room.song.model.SongModel;

public class StartDoubleGameEvent {
    SongModel mSongModel;
    String nextDec;
    boolean hasNext = false;

    public StartDoubleGameEvent(SongModel songModel, String nextDec, boolean hasNext) {
        mSongModel = songModel;
        this.nextDec = nextDec;
        this.hasNext = hasNext;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public SongModel getSongModel() {
        return mSongModel;
    }

    public String getNextDec() {
        return nextDec;
    }
}
