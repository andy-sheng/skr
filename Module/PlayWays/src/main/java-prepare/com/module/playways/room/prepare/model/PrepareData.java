package com.module.playways.room.prepare.model;

import com.module.playways.room.room.model.RankGameConfigModel;
import com.module.playways.room.room.model.RankPlayerInfoModel;
import com.module.playways.room.song.model.SongModel;

import java.io.Serializable;
import java.util.List;

public class PrepareData implements Serializable {
    private int mGameType;
    private String mSysAvatar; // 系统头像
    private SongModel mSongModel;
    private int mGameId;
    private long mGameCreatMs;
    private List<RankPlayerInfoModel> mPlayerInfoList;
    private GameReadyModel mGameReadyInfo;
    private int mShiftTs;
    //一场到底歌曲分类
    private int mTagId;
    private boolean isNewUser = false;

    private String mBgMusic; // 背景音乐

    private RankGameConfigModel mGameConfigModel;// 游戏配置

    private JoinGrabRoomRspModel mGrabCurGameStateModel;
    private String mAgoraToken;

    public JoinGrabRoomRspModel getJoinGrabRoomRspModel() {
        return mGrabCurGameStateModel;
    }

    public void setJoinGrabRoomRspModel(JoinGrabRoomRspModel grabCurGameStateModel) {
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

    public void setPlayerInfoList(List<RankPlayerInfoModel> playerInfoList) {
        mPlayerInfoList = playerInfoList;
    }

    public List<RankPlayerInfoModel> getPlayerInfoList() {
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

    public RankGameConfigModel getGameConfigModel() {
        return mGameConfigModel;
    }

    public void setGameConfigModel(RankGameConfigModel gameConfigModel) {
        mGameConfigModel = gameConfigModel;
    }

    public void setAgoraToken(String agoraToken) {
        mAgoraToken = agoraToken;
    }

    public String getAgoraToken() {
        return mAgoraToken;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }
}
