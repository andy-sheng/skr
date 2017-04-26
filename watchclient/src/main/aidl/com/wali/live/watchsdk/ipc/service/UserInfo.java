package com.wali.live.watchsdk.ipc.service;

import android.os.Parcel;
import android.os.Parcelable;

import com.wali.live.proto.RelationProto;

import java.util.List;

/**
 * Created by zyh on 2017/3/29.
 *
 * @module 提供給宿主app的数据结构
 */
public class UserInfo implements Parcelable {
    private String mUserId;                //uid
    private String mNickname;            //昵称
    private long mAvatar;                //头像
    private String mSignature;           //签名
    private int mGender;                 //性别
    private int mLevel;                  //等级
    private int mBadge;                  //徽章
    private long mUpdateTime;            //更新时间，水位
    private List<Long> mAdminUuids;             //管理员
    private String mCertification;       //认证信息
    private boolean mIsFollowing;        //是否关注 [仅在查询别人的粉丝、关注列表时需要]
    private boolean mIsPushable;         //是否推送 [仅在查询关注列表时需要]
    private boolean mIsBothway;          //是否双向关注 [判断双向关注]
    private int mCertificationType;     //认证类型
    private boolean mIsShowing;          //是否在直播
    private int mViewerCnt;              //直播观众数

    public UserInfo(String userId, String nickname, long avatar, String signature, int gender, int level,
                    int badge, long updateTime, List<Long> adminUuids, String certification, boolean isFollowing,
                    boolean isPushable, boolean isBothway, int certificationType, boolean isShowing, int viewerCnt) {
        this.mUserId = userId;
        this.mNickname = nickname;
        this.mAvatar = avatar;
        this.mSignature = signature;
        this.mGender = gender;
        this.mLevel = level;
        this.mBadge = badge;
        this.mUpdateTime = updateTime;
        this.mAdminUuids = adminUuids;
        this.mCertification = certification;
        this.mIsFollowing = isFollowing;
        this.mIsPushable = isPushable;
        this.mIsBothway = isBothway;
        this.mCertificationType = certificationType;
        this.mIsShowing = isShowing;
        this.mViewerCnt = viewerCnt;
    }

    public UserInfo(RelationProto.UserInfo userInfoProto) {
        this.mUserId = String.valueOf(userInfoProto.getUserId());
        this.mNickname = userInfoProto.getNickname();
        this.mAvatar = userInfoProto.getAvatar();
        this.mSignature = userInfoProto.getSignature();
        this.mGender = userInfoProto.getGender();
        this.mLevel = userInfoProto.getLevel();
        this.mBadge = userInfoProto.getBadge();
        this.mUpdateTime = userInfoProto.getUpdateTime();
        if (userInfoProto.getAdminUidsList() != null) {
            this.mAdminUuids = userInfoProto.getAdminUidsList();
        }
        this.mCertification = userInfoProto.getCertification();
        this.mIsFollowing = userInfoProto.getIsFollowing();
        this.mIsPushable = userInfoProto.getIsPushable();
        this.mIsBothway = userInfoProto.getIsBothway();
        this.mCertificationType = userInfoProto.getCertificationType();
        this.mIsShowing = userInfoProto.getIsShowing();
        this.mViewerCnt = userInfoProto.getViewerCnt();
    }


    public UserInfo(Parcel source) {
        this.mUserId = source.readString();
        this.mNickname = source.readString();
        this.mAvatar = source.readLong();
        this.mSignature = source.readString();
        this.mGender = source.readInt();
        this.mLevel = source.readInt();
        this.mBadge = source.readInt();
        this.mUpdateTime = source.readLong();
        source.readList(this.mAdminUuids, Long.class.getClassLoader());
        this.mCertification = source.readString();
        this.mIsFollowing = source.readByte() != 0;
        this.mIsPushable = source.readByte() != 0;
        this.mIsBothway = source.readByte() != 0;
        this.mCertificationType = source.readInt();
        this.mIsShowing = source.readByte() != 0;
        this.mViewerCnt = source.readInt();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUserId);
        dest.writeString(this.mNickname);
        dest.writeLong(this.mAvatar);
        dest.writeString(this.mSignature);
        dest.writeInt(this.mGender);
        dest.writeInt(this.mLevel);
        dest.writeInt(this.mBadge);
        dest.writeLong(this.mUpdateTime);
        dest.writeList(this.mAdminUuids);
        dest.writeString(this.mCertification);
        dest.writeByte((byte) (this.mIsFollowing ? 1 : 0));
        dest.writeByte((byte) (this.mIsPushable ? 1 : 0));
        dest.writeByte((byte) (this.mIsBothway ? 1 : 0));
        dest.writeInt(this.mCertificationType);
        dest.writeByte((byte) (this.mIsShowing ? 1 : 0));
        dest.writeInt(this.mViewerCnt);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[")
                .append("\nmUserId=" + mUserId)
                .append("\nmNickname=" + mNickname)
                .append("\nmAvatar" + mAvatar)
                .append("\nmSignature" + mSignature)
                .append("\nmGender" + mGender)
                .append("\nmLevel" + mLevel)
                .append("\nmBadge" + mBadge)
                .append("\nmUpdateTime" + mUpdateTime)
                .append("\nmAdminUuids" + mAdminUuids)
                .append("\nmCertification" + mCertification)
                .append("\nmIsFollowing" + mIsFollowing)
                .append("\nmIsPushable" + mIsPushable)
                .append("\nmIsBothway" + mIsBothway)
                .append("\nmCertificationType" + mCertificationType)
                .append("\nmIsShowing" + mIsShowing)
                .append("\nmViewerCnt" + mViewerCnt)
                .append("]");
        return sb.toString();
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public long getAvatar() {
        return mAvatar;
    }

    public void setAvatar(long avatar) {
        mAvatar = avatar;
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        mNickname = nickname;
    }

    public String getSignature() {
        return mSignature;
    }

    public void setSignature(String signature) {
        mSignature = signature;
    }

    public int getGender() {
        return mGender;
    }

    public void setGender(int gender) {
        mGender = gender;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public int getBadge() {
        return mBadge;
    }

    public void setBadge(int badge) {
        mBadge = badge;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        mUpdateTime = updateTime;
    }

    public List<Long> getAdminUuids() {
        return mAdminUuids;
    }

    public void setAdminUuids(List<Long> adminUuids) {
        mAdminUuids = adminUuids;
    }

    public String getCertification() {
        return mCertification;
    }

    public void setCertification(String certification) {
        mCertification = certification;
    }

    public boolean isFollowing() {
        return mIsFollowing;
    }

    public void setFollowing(boolean following) {
        mIsFollowing = following;
    }

    public boolean isPushable() {
        return mIsPushable;
    }

    public void setPushable(boolean pushable) {
        mIsPushable = pushable;
    }

    public boolean isBothway() {
        return mIsBothway;
    }

    public void setBothway(boolean bothway) {
        mIsBothway = bothway;
    }

    public int getCertificationType() {
        return mCertificationType;
    }

    public void setCertificationType(int certificationType) {
        mCertificationType = certificationType;
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void setShowing(boolean showing) {
        mIsShowing = showing;
    }

    public int getViewerCnt() {
        return mViewerCnt;
    }

    public void setViewerCnt(int viewerCnt) {
        mViewerCnt = viewerCnt;
    }
}
