package com.wali.live.watchsdk.fans.model;

import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;

/**
 * Created by zyh on 2017/11/9.
 *
 * @module 群聊详情
 */
public class GroupDetailModel extends BaseViewModel {

    private long mZuid;
    private String mGroupName;
    private int mMemType;
    private int mCharmExp;                 // 魅力值
    private String mCharmTitle;            // 主播魅力值称号
    private int mRanking;                  // 团排名
    private int mMyPetExp;                 // 我的贡献
    private int mMyPetLevel;               // 我的等级
    private int mCurrentMember;            // 团员数
    private int mMemberLimit;              // 团员最大值
    private int mCharmLevel;               // 魅力值等级
    private int mNextCharmExp;             //下一个等级经验值
    private long mVipExpire;               //会员过期时间
    private int mVipLevel;                 //vip等级
    private String mMedalValue;            //团勋章
    private int mPetRanking;               //宠爱值排名

    public GroupDetailModel(VFansProto.GroupDetailRsp rsp) {
        mZuid = rsp.getZuid();
        mGroupName = rsp.getGroupName();
        VFansCommonProto.GroupMemType groupMemType = rsp.getMemType();
        if (groupMemType != null) {
            mMemType = groupMemType.getNumber();
        }
        mCharmExp = rsp.getCharmExp();
        mCharmTitle = rsp.getCharmTitle();
        mRanking = rsp.getRanking();
        mMyPetExp = rsp.getMyPetExp();
        mMyPetLevel = rsp.getMyPetLevel();
        mCurrentMember = rsp.getCurrentMember();
        mMemberLimit = rsp.getMemberLimit();
        mCharmLevel = rsp.getCharmLevel();
        mNextCharmExp = rsp.getNextCharmExp();
        mVipExpire = rsp.getVipExpire();
        mVipLevel = rsp.getVipLevel();
        mMedalValue = rsp.getMedalValue();
        mPetRanking = rsp.getPetRanking();
    }

    public long getZuid() {
        return mZuid;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public int getMemType() {
        return mMemType;
    }

    public int getCharmExp() {
        return mCharmExp;
    }

    public String getCharmTitle() {
        return mCharmTitle;
    }

    public int getRanking() {
        return mRanking;
    }

    public int getMyPetExp() {
        return mMyPetExp;
    }

    public int getMyPetLevel() {
        return mMyPetLevel;
    }

    public int getCurrentMember() {
        return mCurrentMember;
    }

    public int getMemberLimit() {
        return mMemberLimit;
    }

    public int getCharmLevel() {
        return mCharmLevel;
    }

    public int getNextCharmExp() {
        return mNextCharmExp;
    }

    public long getVipExpire() {
        return mVipExpire;
    }

    public int getVipLevel() {
        return mVipLevel;
    }

    public String getMedalValue() {
        return mMedalValue;
    }

    public int getPetRanking() {
        return mPetRanking;
    }

    @Override
    public String toString() {
        return "GroupDetailModel{" +
                "mZuid=" + mZuid +
                ", mGroupName='" + mGroupName + '\'' +
                ", mMemType=" + mMemType +
                ", mCharmExp=" + mCharmExp +
                ", mCharmTitle='" + mCharmTitle + '\'' +
                ", mRanking=" + mRanking +
                ", mMyPetExp=" + mMyPetExp +
                ", mMyPetLevel=" + mMyPetLevel +
                ", mCurrentMember=" + mCurrentMember +
                ", mMemberLimit=" + mMemberLimit +
                ", mCharmLevel=" + mCharmLevel +
                ", mNextCharmExp=" + mNextCharmExp +
                ", mVipExpire=" + mVipExpire +
                ", mVipLevel=" + mVipLevel +
                ", mMedalValue='" + mMedalValue + '\'' +
                ", mPetRanking=" + mPetRanking +
                '}';
    }
}
