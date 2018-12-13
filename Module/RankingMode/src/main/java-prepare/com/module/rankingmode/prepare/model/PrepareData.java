package com.module.rankingmode.prepare.model;

import com.module.rankingmode.song.model.SongModel;

import java.io.Serializable;
import java.util.List;

public class PrepareData implements Serializable {
    private SongModel mSongModel;
    private int mGameId;
    private long mGameCreatMs;
    private List<PlayerInfo> mPlayerInfoList;

    public void setSongModel(SongModel songModel) {
        mSongModel = songModel;
    }

    public SongModel getSongModel() {
        return mSongModel;
    }

    public void setGameId(int gameId) {
        mGameId = gameId;
    }

    public int getGameId() {
        return mGameId;
    }

    public void setGameCreatMs(long gameCreatMs) {
        mGameCreatMs = gameCreatMs;
    }

    public long getGameCreatMs() {
        return mGameCreatMs;
    }

    public void setPlayerInfoList(List<PlayerInfo> playerInfoList) {
        mPlayerInfoList = playerInfoList;
    }

    public List<PlayerInfo> getPlayerInfoList() {
        return mPlayerInfoList;
    }
}
