package com.wali.live.watchsdk.fans.model;

import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;

/**
 * Created by zhangyuehuan on 2017/11/9.
 *
 * @module 粉丝成员数据体
 */
public class MemberInfoModel extends BaseViewModel {
    private long mUid;
    private String mNickname;
    private long mAvatar;
    private int mPetExp;
    private int mPetLevel;
    private int mMemType;
    private String mMedalName;
    private boolean mIsFollow;
    private boolean mIsBothWay;
    private String mSign;
    private boolean mIsBeForbid;
    private int mVipType;

    public MemberInfoModel(VFansProto.MemberInfo info) {
        mUid = info.getUuid();
        mNickname = info.getNickname();
        mPetExp = info.getPetExp();
        mPetLevel = info.getPetLevel();
        mMemType = info.getMemType().getNumber();
        mMedalName = info.getMedalValue();
        mAvatar = info.getAvatar();
        mIsBothWay = info.getIsBothfollowing();
        mIsFollow = info.getIsFollowing();
        mSign = info.getSign();
        mIsBeForbid = info.getForbidNoise();
        mVipType = info.getVipType();
    }

    public long getUid() {
        return mUid;
    }

    public String getNickname() {
        return mNickname;
    }

    public long getAvatar() {
        return mAvatar;
    }

    public int getPetExp() {
        return mPetExp;
    }

    public int getPetLevel() {
        return mPetLevel;
    }

    public int getMemType() {
        return mMemType;
    }

    public String getMedalName() {
        return mMedalName;
    }

    public boolean isFollow() {
        return mIsFollow;
    }

    public boolean isBothWay() {
        return mIsBothWay;
    }

    public String getSign() {
        return mSign;
    }

    public boolean isBeForbid() {
        return mIsBeForbid;
    }

    public int getVipType() {
        return mVipType;
    }

    @Override
    public String toString() {
        return "MemberInfoModel{" +
                "mUid=" + mUid +
                ", mNickname='" + mNickname + '\'' +
                ", mAvatar=" + mAvatar +
                ", mPetExp=" + mPetExp +
                ", mPetLevel=" + mPetLevel +
                ", mMemType=" + mMemType +
                ", mMedalName='" + mMedalName + '\'' +
                ", mIsFollow=" + mIsFollow +
                ", mIsBothWay=" + mIsBothWay +
                ", mSign='" + mSign + '\'' +
                ", mIsBeForbid=" + mIsBeForbid +
                ", mVipType=" + mVipType +
                '}';
    }
}
