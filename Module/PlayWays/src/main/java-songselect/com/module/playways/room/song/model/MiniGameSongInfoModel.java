package com.module.playways.room.song.model;

import com.zq.live.proto.Common.MiniGameSongInfo;

import java.io.Serializable;

public class MiniGameSongInfoModel implements Serializable {
    String songURL;
    String songName;

    public String getSongURL() {
        return songURL;
    }

    public void setSongURL(String songURL) {
        this.songURL = songURL;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public static MiniGameSongInfoModel parse(MiniGameSongInfo miniGameSongInfo) {
        MiniGameSongInfoModel miniGameSongInfoModel = new MiniGameSongInfoModel();
        miniGameSongInfoModel.setSongName(miniGameSongInfo.getSongName());
        miniGameSongInfoModel.setSongURL(miniGameSongInfo.getSongURL());
        return miniGameSongInfoModel;
    }

    @Override
    public String toString() {
        return "MiniGameSongInfo{" +
                "songURL='" + songURL + '\'' +
                ", songName='" + songName + '\'' +
                '}';
    }
}
