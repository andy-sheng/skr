package com.wali.live.watchsdk.watch.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

/**
 * Created by lan on 16/11/29.
 *
 * @notice 采取小设计，不需要的属性不要加
 */
@Keep
public class RoomInfo implements Parcelable {
    private long mPlayerId;
    private String mLiveId;
    private String mVideoUrl;
    private long mStartTime;

    // 什么类型的直播，有公开的／私密的／口令／游戏的 列表中会给出每个项目的直播类型
    private int mLiveType;
    // 以下是user内的非直播必须的信息，如果需要完整的信息，以后直接传User进来
    private long mAvatar;

    // 游戏直播相关的，表明对应的游戏
    private String mGameId;

    // 是否支持分享
    private boolean mEnableShare;

    // 是否支持关注
    private boolean mEnableFollow = true;

    // 以下与ui相关的信息
    private String mCoverUrl;

    private RoomInfo(long playerId, String liveId, String videoUrl) {
        mPlayerId = playerId;
        mLiveId = liveId;
        mVideoUrl = videoUrl;
    }

    protected RoomInfo(Parcel in) {
        mPlayerId = in.readLong();
        mLiveId = in.readString();
        mVideoUrl = in.readString();
        mAvatar = in.readLong();
        mCoverUrl = in.readString();
        mStartTime = in.readLong();
        mLiveType = in.readInt();
        mGameId = in.readString();
        mEnableShare = in.readByte() != 0;
        mEnableFollow = in.readByte() != 0;
    }

    public static final Creator<RoomInfo> CREATOR = new Creator<RoomInfo>() {
        @Override
        public RoomInfo createFromParcel(Parcel in) {
            return new RoomInfo(in);
        }

        @Override
        public RoomInfo[] newArray(int size) {
            return new RoomInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 1;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mPlayerId);
        parcel.writeString(mLiveId);
        parcel.writeString(mVideoUrl);
        parcel.writeLong(mAvatar);
        parcel.writeString(mCoverUrl);
        parcel.writeLong(mStartTime);
        parcel.writeInt(mLiveType);
        parcel.writeString(mGameId);
        parcel.writeByte((byte) (this.mEnableShare ? 1 : 0));
        parcel.writeByte((byte) (this.mEnableFollow ? 1: 0));
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long mStartTime) {
        this.mStartTime = mStartTime;
    }

    public void setAvatar(long avatar) {
        mAvatar = avatar;
    }

    public void setCoverUrl(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    public long getPlayerId() {
        return mPlayerId;
    }

    public String getLiveId() {
        return mLiveId;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    public long getAvatar() {
        return mAvatar;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public void setPlayerId(long playerId) {
        mPlayerId = playerId;
    }

    public void setLiveId(String liveId) {
        mLiveId = liveId;
    }

    public void setVideoUrl(String videoUrl) {
        mVideoUrl = videoUrl;
    }

    public void setLiveType(int liveType) {
        this.mLiveType = liveType;
    }

    public int getLiveType() {
        return mLiveType;
    }

    public String getGameId() {
        return mGameId;
    }

    public void setGameId(String gameId) {
        mGameId = gameId;
    }

    public boolean isEnableShare() {
        return mEnableShare;
    }

    public void setEnableShare(boolean enableShare) {
        mEnableShare = enableShare;
    }

    public boolean isEnableFollow() {
        return mEnableFollow;
    }

    public void setEnableFollow(boolean enableFollow) {
        mEnableFollow = enableFollow;
    }

    @Keep
    public static class Builder {
        private RoomInfo mRoomInfo;

        public static Builder newInstance(long playerId, String liveId, String videoUrl) {
            return new Builder().setBasicInfo(playerId, liveId, videoUrl);
        }

        private Builder setBasicInfo(long playerId, String liveId, String videoUrl) {
            mRoomInfo = new RoomInfo(playerId, liveId, videoUrl);
            return this;
        }

        public Builder setAvatar(long avatar) {
            mRoomInfo.setAvatar(avatar);
            return this;
        }

        public Builder setStartTime(long startTime) {
            mRoomInfo.setStartTime(startTime);
            return this;
        }

        public Builder setCoverUrl(String coverUrl) {
            mRoomInfo.setCoverUrl(coverUrl);
            return this;
        }

        public Builder setLiveType(int livetype) {
            mRoomInfo.setLiveType(livetype);
            return this;
        }

        public Builder setGameId(String gameId) {
            mRoomInfo.setGameId(gameId);
            return this;
        }

        public Builder setEnableShare(boolean enableShare) {
            mRoomInfo.setEnableShare(enableShare);
            return this;
        }

        public Builder setEnableFollow(boolean enableFollow){
            mRoomInfo.setEnableFollow(enableFollow);
            return this;
        }

        public RoomInfo build() {
            return mRoomInfo;
        }
    }

    @Override
    public String toString() {
        return "RoomInfo{" +
                "mPlayerId=" + mPlayerId +
                ", mLiveId='" + mLiveId + '\'' +
                ", mVideoUrl='" + mVideoUrl + '\'' +
                ", mStartTime=" + mStartTime +
                ", mLiveType=" + mLiveType +
                ", mAvatar=" + mAvatar +
                ", mGameId='" + mGameId + '\'' +
                ", mEnableShare=" + mEnableShare +
                ", mEnableFollow=" + mEnableFollow +
                ", mCoverUrl='" + mCoverUrl + '\'' +
                '}';
    }
}
