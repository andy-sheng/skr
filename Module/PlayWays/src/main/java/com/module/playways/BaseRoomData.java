package com.module.playways;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.BaseRoundInfoModel;
import com.module.playways.rank.song.model.SongModel;

import java.io.Serializable;
import java.util.List;

import io.reactivex.subjects.PublishSubject;


/**
 * 房间内所有数据的聚合类
 * 每种模式的房间内状态信息都由其存储
 */
public abstract class BaseRoomData<T extends BaseRoundInfoModel> implements Serializable {
    public final static String TAG = "RoomData";

    public final static String RANK_BATTLE_START_SVGA = "http://res-static.inframe.mobi/app/rank_battle_start.svga";
    public final static String RANK_RESULT_WIN_SVGA = "http://res-static.inframe.mobi/app/rank_result_win.svga";
    public final static String RANK_RESULT_LOSE_SVGA = "http://res-static.inframe.mobi/app/rank_result_lose.svga";
    public final static String RANK_RESULT_DRAW_SVGA = "http://res-static.inframe.mobi/app/rank_result_draw.svga";
    public final static String GRAB_BURST_BIG_SVGA = "http://res-static.inframe.mobi/app/grab_burst_big_animation.svga";

    public final static String PK_MAIN_STAGE_WEBP = "http://res-static.inframe.mobi/app/pk_main_stage.webp";
    public final static String READY_GO_SVGA_URL = "http://res-static.inframe.mobi/app/sige_go.svga";
    public final static String ROOM_STAGE_SVGA = "http://res-static.inframe.mobi/app/main_stage_people.svga";
    public final static String ROOM_SPECAIL_EMOJI_DABIAN = "http://res-static.inframe.mobi/app/emoji_bianbian.svga";
    public final static String ROOM_SPECAIL_EMOJI_AIXIN = "http://res-static.inframe.mobi/app/emoji_love.svga";
    public static final String AUDIO_FOR_AI_PATH = "audioforai.aac";
    public static final String MATCHING_SCORE_FOR_AI_PATH = "matchingscore.json";

    protected int mGameId; // 房间id

    protected String mSysAvatar; // 系统头像

    /**
     * 当要拿服务器时间和本地时间比较时，请将服务器时间加上这个矫正值
     * 如
     * if(System.currentTimeMillis() > mGameStartTs + mShiftts){
     * <p>
     * }
     */
    protected int mShiftTs;// 本地时间比服务器快多少毫秒，比如快1秒，mShiftTs = 1000;

    protected long mGameCreateTs;// 游戏创建时间,服务器的

    protected long mGameStartTs;// 游戏开始时间,服务器的

    protected long mGameOverTs;// 游戏结束时间,服务器的

    protected long mLastSyncTs;// 上次同步服务器状态时间,服务器的

    protected SongModel mSongModel; // 歌曲信息

    protected T mExpectRoundInfo;// 按理的 期望的当前的轮次

    protected T mRealRoundInfo;// 实际的当前轮次信息

    protected boolean mIsGameFinish = false; // 游戏开始了

    protected boolean mMute = false;//是否mute

    private String mAgoraToken; // 声网token

    public abstract int getGameType();

    public abstract void checkRoundInEachMode();

    public void setIsGameFinish(boolean isGameFinish) {
        this.mIsGameFinish = isGameFinish;
    }

    public boolean isIsGameFinish() {
        return mIsGameFinish;
    }

    public int getGameId() {
        return mGameId;
    }

    public void setGameId(int gameId) {
        mGameId = gameId;
    }

    public String getSysAvatar() {
        return mSysAvatar;
    }

    public void setSysAvatar(String sysAvatar) {
        mSysAvatar = sysAvatar;
    }

    public int getShiftTs() {
        return mShiftTs;
    }

    public void setShiftTs(int shiftTs) {
        mShiftTs = shiftTs;
    }

    public long getGameCreateTs() {
        return mGameCreateTs;
    }

    public void setGameCreateTs(long gameCreateTs) {
        mGameCreateTs = gameCreateTs;
    }

    public long getGameStartTs() {
        return mGameStartTs;
    }

    public void setGameStartTs(long gameStartTs) {
        mGameStartTs = gameStartTs;
    }

    public long getGameOverTs() {
        return mGameOverTs;
    }

    public void setGameOverTs(long gameOverTs) {
        mGameOverTs = gameOverTs;
    }

    public long getLastSyncTs() {
        return mLastSyncTs;
    }

    public void setLastSyncTs(long lastSyncTs) {
        mLastSyncTs = lastSyncTs;
    }

    public SongModel getSongModel() {
        return mSongModel;
    }

    public void setSongModel(SongModel songModel) {
        mSongModel = songModel;
    }

    public T getExpectRoundInfo() {
        return mExpectRoundInfo;
    }

    public void setExpectRoundInfo(T expectRoundInfo) {
        mExpectRoundInfo = expectRoundInfo;
    }

    public <T extends BaseRoundInfoModel> T getRealRoundInfo() {
        return (T) mRealRoundInfo;
    }

    public void setRealRoundInfo(T realRoundInfo) {
        mRealRoundInfo = realRoundInfo;
    }


    public int getRealRoundSeq() {
        if (mRealRoundInfo != null) {
            return mRealRoundInfo.getRoundSeq();
        }

        return -1;
    }

    public boolean isMute() {
        return mMute;
    }

    public void setMute(boolean mute) {
        mMute = mute;
    }

    @Override
    public String toString() {
        return "RoomData{" +
                "mGameType=" + getGameType() +
                ", mGameId=" + mGameId +
                ", mSysAvatar='" + mSysAvatar + '\'' +
                ", mShiftTs=" + mShiftTs +
                ", mGameCreateTs=" + mGameCreateTs +
                ", mGameStartTs=" + mGameStartTs +
                ", mGameOverTs=" + mGameOverTs +
                ", mLastSyncTs=" + mLastSyncTs +
                ", mSongModel=" + mSongModel +
                ", mExpectRoundInfo=" + mExpectRoundInfo +
                ", mRealRoundInfo=" + mRealRoundInfo +
                ", mIsGameFinish=" + mIsGameFinish +
                ", mMute=" + mMute +
                '}';
    }

    public abstract <T extends PlayerInfoModel> List<T> getPlayerInfoList();

    public UserInfoModel getUserInfo(int userID) {
        if (userID == 0) {
            return null;
        }
        List<PlayerInfoModel> l = getPlayerInfoList();
        if (l == null) {
            return null;
        }
        for (PlayerInfoModel playerInfo : l) {
            if (playerInfo.getUserInfo().getUserId() == userID) {
                return playerInfo.getUserInfo();
            }
        }
        return null;
    }

    public void setAgoraToken(String agoraToken) {
        mAgoraToken = agoraToken;
    }

    public String getAgoraToken() {
        return mAgoraToken;
    }
}
