package com.module.playways.doubleplay;

import com.module.playways.room.song.model.SongModel;

import java.io.Serializable;

public class DoubleRoundInfo implements Serializable {
    protected int roundSeq;
    SongModel mSongModel;

    public int getRoundSeq() {
        return roundSeq;
    }

    public SongModel getSongModel() {
        return mSongModel;
    }

    public void update(DoubleRoundInfo roundInfo) {

    }
}
