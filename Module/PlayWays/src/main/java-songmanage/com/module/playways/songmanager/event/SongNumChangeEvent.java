package com.module.playways.songmanager.event;

public class SongNumChangeEvent {
    int songNum = -1;

    public SongNumChangeEvent(int songNum) {
        this.songNum = songNum;
    }

    public int getSongNum() {
        return songNum;
    }
}
