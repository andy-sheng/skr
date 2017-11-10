package com.wali.live.watchsdk.fans.model.member;

import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

/**
 * Created by lan on 2017/11/10.
 */
public class FansMemberModel extends BaseViewModel {
    private long mUuid;
    private String mNickname;
    private long mAvatar;
    private int mPetExp;
    private int mPetLevel;
    private int mMemType;
    private int mItemType;
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
        this.mUuid = protoMember.getUuid();
        this.mNickname = protoMember.getNickname();
        this.mPetExp = protoMember.getPetExp();
        this.mPetLevel = protoMember.getPetLevel();
        this.mMemType = protoMember.getMemType().getNumber();
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

    public int getItemType() {
        return mItemType;
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
}
