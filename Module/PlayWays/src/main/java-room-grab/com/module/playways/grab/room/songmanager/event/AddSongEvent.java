package com.module.playways.grab.room.songmanager.event;

import com.module.playways.room.song.model.SongModel;

public class AddSongEvent {
    SongModel mSongModel;

    public AddSongEvent(SongModel songModel) {
        mSongModel = songModel;
    }

    public SongModel getSongModel() {
        return mSongModel;
    }
}
