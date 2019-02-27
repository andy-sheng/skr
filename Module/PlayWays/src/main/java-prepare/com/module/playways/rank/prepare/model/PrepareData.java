package com.module.playways.rank.prepare.model;

import com.module.playways.rank.song.model.SongModel;

import java.io.Serializable;
import java.util.List;

public class PrepareData implements Serializable {
    private int mGameType;
    private String mSysAvatar; // 系统头像
    private SongModel mSongModel;
    private int mGameId;
    private long mGameCreatMs;
    private List<PlayerInfoModel> mPlayerInfoList;
    private GameReadyModel mGameReadyInfo;
    private int mShiftTs;
    //一场到底歌曲分类
    private int mTagId;

    private String mBgMusic; // 背景音乐

    private GameConfigModel mGameConfigModel;// 游戏配置

    private GrabCurGameStateModel mGrabCurGameStateModel;

    public GrabCurGameStateModel getGrabCurGameStateModel() {
        return mGrabCurGameStateModel;
    }

    public void setGrabCurGameStateModel(GrabCurGameStateModel grabCurGameStateModel) {
        mGrabCurGameStateModel = grabCurGameStateModel;
    }

    public int getTagId() {
        return mTagId;
    }

    public void setTagId(int tagId) {
        this.mTagId = tagId;
    }

    //一场到低所有歌曲
    private List<SongModel> mSongModelList;

    public int getGameType() {
        return mGameType;
    }

    public void setGameType(int gameType) {
        mGameType = gameType;
    }

    public String getSysAvatar() {
        return mSysAvatar;
    }

    public void setSysAvatar(String sysAvatar) {
        this.mSysAvatar = sysAvatar;
    }

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

    public List<SongModel> getSongModelList() {
        return mSongModelList;
    }

    public void setSongModelList(List<SongModel> songModelList) {
        mSongModelList = songModelList;
    }

    public long getGameCreatMs() {
        return mGameCreatMs;
    }

    public void setPlayerInfoList(List<PlayerInfoModel> playerInfoList) {
        mPlayerInfoList = playerInfoList;
    }

    public List<PlayerInfoModel> getPlayerInfoList() {
        return mPlayerInfoList;
    }

    public void setGameReadyInfo(GameReadyModel gameReadyInfo) {
        mGameReadyInfo = gameReadyInfo;
    }

    public GameReadyModel getGameReadyInfo() {
        return mGameReadyInfo;
    }

    public void setShiftTs(int shiftTs) {
        this.mShiftTs = shiftTs;
    }

    public int getShiftTs() {
        return mShiftTs;
    }


    public String getBgMusic() {
        return mBgMusic;
    }

    public void setBgMusic(String bgMusic) {
        this.mBgMusic = bgMusic;
    }

    public GameConfigModel getGameConfigModel() {
        return mGameConfigModel;
    }

    public void setGameConfigModel(GameConfigModel gameConfigModel) {
        mGameConfigModel = gameConfigModel;
    }
}
