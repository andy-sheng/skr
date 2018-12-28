package com.module.playways.rank.prepare.model;

import com.module.playways.rank.song.model.SongModel;

import java.io.Serializable;
import java.util.List;

public class PrepareData implements Serializable {
    private SongModel mSongModel;
    private int mGameId;
    private long mGameCreatMs;
    private List<PlayerInfo> mPlayerInfoList;
    private GameReadyModel mGameReadyInfo;
    private int shiftTs;

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

    public void setGameReadyInfo(GameReadyModel gameReadyInfo) {
        mGameReadyInfo = gameReadyInfo;
    }

    public GameReadyModel getGameReadyInfo() {
        return mGameReadyInfo;
    }

    public void setShiftTs(int shiftTs) {
        this.shiftTs = shiftTs;
    }

    public int getShiftTs() {
        return shiftTs;
    }
}
