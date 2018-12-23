package com.module.rankingmode.room.model;

import com.common.core.account.UserAccountManager;
import com.common.core.userinfo.UserInfo;
import com.common.log.MyLog;
import com.module.rankingmode.prepare.model.OnLineInfoModel;
import com.module.rankingmode.prepare.model.PlayerInfo;
import com.module.rankingmode.prepare.model.RoundInfoModel;
import com.module.rankingmode.room.event.RoundInfoChangeEvent;
import com.module.rankingmode.song.model.SongModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RoomData {
    public final static String TAG = "RoomData";

    public final static String READY_GO_WEBP_URL = "http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/ready_go4.webp";

    private int mGameId; // 房间id

    /**
     * 当要拿服务器时间和本地时间比较时，请将服务器时间加上这个矫正值
     * 如
     * if(System.currentTimeMillis() > mGameStartTs + mShiftts){
     * <p>
     * }
     */
    private int mShiftTs;// 本地时间比服务器快多少毫秒，比如快1秒，mShiftTs = 1000;

    private long mGameCreateTs;// 游戏创建时间,服务器的

    private long mGameStartTs;// 游戏开始时间,服务器的

    private long mGameOverTs;// 游戏结束时间,服务器的

    private long mLastSyncTs;// 上次同步服务器状态时间,服务器的

    private SongModel mSongModel; // 歌曲信息

    private List<RoundInfoModel> mRoundInfoModelList;//所有的轮次信息

    private RoundInfoModel mExpectRoundInfo;// 按理的 期望的当前的轮次

    private RoundInfoModel mRealRoundInfo;// 实际的当前轮次信息

    private List<OnLineInfoModel> mOnlineInfoList;//所有的用户在线信息

    private List<PlayerInfo> mPlayerInfoList;//选手信息

    private volatile boolean mIsGameFinish = false;

    /**
     * 检查轮次信息是否需要更新
     */
    public void checkRound() {
        MyLog.d(TAG, "checkRound mExcpectRoundInfo=" + mExpectRoundInfo + " mRealRoundInfo=" + mRealRoundInfo);
        if (mIsGameFinish) {
            MyLog.d(TAG, "游戏结束了，不需要再check");
            return;
        }
        if (mExpectRoundInfo == null) {
            // 结束状态了
            if (mRealRoundInfo != null) {
                mRealRoundInfo = null;
                EventBus.getDefault().post(new RoundInfoChangeEvent(false));
            }
            return;
        }
        if (!RoomDataUtils.roundInfoEqual(mExpectRoundInfo, mRealRoundInfo)) {
            // 轮次需要更新了
            RoundInfoModel oldRoundInfo = mRealRoundInfo;
            mRealRoundInfo = mExpectRoundInfo;
            if (mRealRoundInfo.getUserID() == UserAccountManager.getInstance().getUuidAsLong()) {
                // 轮到自己唱了。开始发心跳，开始倒计时，3秒后 开始开始混伴奏，开始解除引擎mute，
                EventBus.getDefault().post(new RoundInfoChangeEvent(true));
            } else {
                // 别人唱，本人的引擎mute，取消本人心跳。监听他人的引擎是否 unmute,开始混制歌词
                EventBus.getDefault().post(new RoundInfoChangeEvent(false));
            }
        }
    }

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

    public List<RoundInfoModel> getRoundInfoModelList() {
        return mRoundInfoModelList;
    }

    public void setRoundInfoModelList(List<RoundInfoModel> roundInfoModelList) {
        mRoundInfoModelList = roundInfoModelList;
    }

    public RoundInfoModel getExpectRoundInfo() {
        return mExpectRoundInfo;
    }

    public void setExpectRoundInfo(RoundInfoModel expectRoundInfo) {
        mExpectRoundInfo = expectRoundInfo;
    }

    public RoundInfoModel getRealRoundInfo() {
        return mRealRoundInfo;
    }

    public void setRealRoundInfo(RoundInfoModel realRoundInfo) {
        mRealRoundInfo = realRoundInfo;
    }

    public List<OnLineInfoModel> getOnlineInfoList() {
        return mOnlineInfoList;
    }

    public void setOnlineInfoList(List<OnLineInfoModel> onlineInfoList) {
        mOnlineInfoList = onlineInfoList;
    }

    public void setPlayerInfoList(List<PlayerInfo> playerInfoList) {
        mPlayerInfoList = playerInfoList;
    }

    public List<PlayerInfo> getPlayerInfoList() {
        return mPlayerInfoList;
    }

    public UserInfo getUserInfo(int userID) {
        if (userID == 0) {
            return null;
        }
        if (mPlayerInfoList == null) {
            return null;
        }
        for (PlayerInfo playerInfo : mPlayerInfoList) {
            if (playerInfo.getUserInfo().getUserId() == userID) {
                return playerInfo.getUserInfo();
            }
        }

        return null;
    }
}
