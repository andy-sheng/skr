package com.module.playways.doubleplay.event;

import com.module.playways.room.song.model.SongModel;

public class StartDoubleGameEvent {
    SongModel mSongModel;
    String nextDec;

    public StartDoubleGameEvent(SongModel songModel, String nextDec) {
        mSongModel = songModel;
        this.nextDec = nextDec;
    }

    public SongModel getSongModel() {
        return mSongModel;
    }

    public String getNextDec() {
        return nextDec;
    }
}
