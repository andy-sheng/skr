package com.wali.live.watchsdk.fans.model.member;

import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

/**
 * Created by lan on 2017/11/10.
 */
public class FansMemberModel extends BaseViewModel {
    public static final int VIP_TYPE_NO = 0;    // 没有
    public static final int VIP_TYPE_MONTH = 1; // 月费
    public static final int VIP_TYPE_YEAR = 2;  // 年费

    private long mUuid;
    private String mNickname;
    private long mAvatar;
    private int mPetExp;
    private int mPetLevel;
    private int mMemType;

    private String mMedalName;      // 等级对应的勋章名字
    private boolean mIsFollow;
    private boolean mIsBothWay;
    private String mSign;
    private boolean mIsForbid;      // 是否被禁言
    private int mVipType;           // vip等级
    private int mNewLoveValue;

    public FansMemberModel(VFansProto.MemberInfo protoMember) {
        parse(protoMember);
    }

    public void parse(VFansProto.MemberInfo protoMember) {
        mUuid = protoMember.getUuid();
        mNickname = protoMember.getNickname();
        mPetExp = protoMember.getPetExp();
        mPetLevel = protoMember.getPetLevel();
        mMemType = protoMember.getMemType().getNumber();

        mMedalName = protoMember.getMedalValue();
        mAvatar = protoMember.getAvatar();
        mIsBothWay = protoMember.getIsBothfollowing();
        mIsFollow = protoMember.getIsFollowing();
        mSign = protoMember.getSign();
        mIsForbid = protoMember.getForbidNoise();
        mVipType = protoMember.getVipType();
        mNewLoveValue = protoMember.getLast7DaysExp();
    }

    public long getUuid() {
        return mUuid;
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

    public boolean isForbid() {
        return mIsForbid;
    }

    public int getVipType() {
        return mVipType;
    }

    public int getNewLoveValue() {
        return mNewLoveValue;
    }

    @Override
    public String toString() {
        return "FansMemberModel{" +
                "mUuid=" + mUuid +
                ", mNickname='" + mNickname + '\'' +
                ", mAvatar=" + mAvatar +
                ", mPetExp=" + mPetExp +
                ", mPetLevel=" + mPetLevel +
                ", mMemType=" + mMemType +
                ", mMedalName='" + mMedalName + '\'' +
                ", mIsFollow=" + mIsFollow +
                ", mIsBothWay=" + mIsBothWay +
                ", mSign='" + mSign + '\'' +
                ", mIsForbid=" + mIsForbid +
                ", mVipType=" + mVipType +
                ", mNewLoveValue=" + mNewLoveValue +
                '}';
    }
}
