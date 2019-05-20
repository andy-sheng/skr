package com.module.playways.grab.room.model;

import com.module.playways.room.song.model.SongModel;

import java.io.Serializable;

public class WonderfulMomentModel implements Serializable {
    String localPath;
    SongModel mSongModel;

    public WonderfulMomentModel(String localPath, SongModel songModel) {
        this.localPath = localPath;
        mSongModel = songModel;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public SongModel getSongModel() {
        return mSongModel;
    }

    public void setSongModel(SongModel songModel) {
        mSongModel = songModel;
    }
}
