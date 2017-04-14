package com.wali.live.watchsdk.ipc.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zyh on 2017/3/29.
 *
 * @module 提供給宿主app的数据结构
 */
public class LiveInfo implements Parcelable {
    private String mLiveId;  //直播id
    //主播信息
    private long mUserId;       //用户id
    private String mNickname;   //昵称
    private long mAvatar;       //头像时间戳
    private int mLevel;         //等级
    private int mCertType;      //认证类型
    //
    private String mCity;       //地点
    private int mViewerCnt;     //观众数
    private String mUrl;        //直播地址
    private String mCoverUrl;   //房间的封面图片地址
    private String mLiveTitle;  //直播标题
    private long mStartTime;    //直播开始时间
    private String mTag;        //个性化标签(打点上报)
    private int mAppType = 11;  //0=小米直播app, 1=无人机, 2=导播台, 3=游戏, 4=一直播
    private int mLiveType = 12; //直播类型[和房间接口一致]. 0=公开, 1=私密, 2=口令, 3=门票

    public LiveInfo(String liveId, long userId, String nickname, long avatar, int level, int certType, String city, int viewerCnt,
                    String url, String coverUrl, String liveTitle, long startTime, String tag, int appType, int liveType) {
        this.mLiveId = liveId;
        this.mUserId = userId;
        this.mNickname = nickname;
        this.mAvatar = avatar;
        this.mLevel = level;
        this.mCertType = certType;
        this.mCity = city;
        this.mViewerCnt = viewerCnt;
        this.mUrl = url;
        this.mCoverUrl = coverUrl;
        this.mLiveTitle = liveTitle;
        this.mStartTime = startTime;
        this.mTag = tag;
        this.mAppType = appType;
        this.mLiveType = liveType;
    }

    protected LiveInfo(Parcel in) {
        this.mLiveId = in.readString();
        this.mUserId = in.readLong();
        this.mNickname = in.readString();
        this.mAvatar = in.readLong();
        this.mLevel = in.readInt();
        this.mCertType = in.readInt();
        this.mCity = in.readString();
        this.mViewerCnt = in.readInt();
        this.mUrl = in.readString();
        this.mCoverUrl = in.readString();
        this.mLiveTitle = in.readString();
        this.mStartTime = in.readLong();
        this.mTag = in.readString();
        this.mAppType = in.readInt();
        this.mLiveType = in.readInt();
    }

    public String getLiveId() {
        return mLiveId;
    }

    public void setLiveId(String liveId) {
        mLiveId = liveId;
    }

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(long userId) {
        mUserId = userId;
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        mNickname = nickname;
    }

    public long getAvatar() {
        return mAvatar;
    }

    public void setAvatar(long avatar) {
        mAvatar = avatar;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public int getCertType() {
        return mCertType;
    }

    public void setCertType(int certType) {
        mCertType = certType;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public int getViewerCnt() {
        return mViewerCnt;
    }

    public void setViewerCnt(int viewerCnt) {
        mViewerCnt = viewerCnt;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    public String getLiveTitle() {
        return mLiveTitle;
    }

    public void setLiveTitle(String liveTitle) {
        mLiveTitle = liveTitle;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public int getAppType() {
        return mAppType;
    }

    public void setAppType(int appType) {
        mAppType = appType;
    }

    public int getLiveType() {
        return mLiveType;
    }

    public void setLiveType(int liveType) {
        mLiveType = liveType;
    }

    public static final Creator<LiveInfo> CREATOR = new Creator<LiveInfo>() {
        @Override
        public LiveInfo createFromParcel(Parcel source) {
            return new LiveInfo(source);
        }

        @Override
        public LiveInfo[] newArray(int size) {
            return new LiveInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLiveId);
        dest.writeLong(this.mUserId);
        dest.writeString(this.mNickname);
        dest.writeLong(this.mAvatar);
        dest.writeInt(this.mLevel);
        dest.writeInt(this.mCertType);
        dest.writeString(this.mCity);
        dest.writeInt(this.mViewerCnt);
        dest.writeString(this.mUrl);
        dest.writeString(this.mCoverUrl);
        dest.writeString(this.mLiveTitle);
        dest.writeLong(this.mStartTime);
        dest.writeString(this.mTag);
        dest.writeInt(this.mAppType);
        dest.writeInt(this.mLiveType);

    }
}
