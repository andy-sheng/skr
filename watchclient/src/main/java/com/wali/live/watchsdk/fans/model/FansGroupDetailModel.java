package com.wali.live.watchsdk.fans.model;

import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.item.MemFansGroupModel;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

/**
 * Created by lan on 2017/11/9.
 */
public class FansGroupDetailModel extends BaseViewModel {
    private long mZuid;
    private String mGroupName;
    
    private int mMemType;

    private int mCharmExp;          // 魅力值
    private String mCharmTitle;     // 主播魅力值称号
    private int mRanking;           // 团排名
    private int mMyPetExp;          // 我的贡献
    private int mMyPetLevel;        // 我的等级
    private int mCurrentMember;     // 团员数
    private int mMemberLimit;       // 团员最大值
    private int mCharmLevel;        // 魅力值等级
    private int mNextCharmExp;      // 下一个等级经验值
    private long mVipExpire;        // 会员过期时间
    private int mVipLevel;          // vip等级
    private String mMedalValue;     // 团勋章
    private int mPetRanking;        // 宠爱值排名

    public FansGroupDetailModel(VFansProto.GroupDetailRsp rsp) {
        parse(rsp);
    }

    public FansGroupDetailModel(MemFansGroupModel memGroupModel) {
        mZuid = memGroupModel.getZuid();
        mGroupName = memGroupModel.getGroupName();

        mMyPetExp = memGroupModel.getPetExp();
        mMyPetLevel = memGroupModel.getPetLevel();

        mVipExpire = memGroupModel.getVipExpire();
        mVipLevel = memGroupModel.getVipLevel();

        mMedalValue = memGroupModel.getMedalValue();
    }

    public void parse(VFansProto.GroupDetailRsp rsp) {
        this.mZuid = rsp.getZuid();
        this.mGroupName = rsp.getGroupName();

        this.mMemType = rsp.getMemType().getNumber();

        this.mCharmExp = rsp.getCharmExp();
        this.mCharmTitle = rsp.getCharmTitle();
        this.mRanking = rsp.getRanking();
        this.mMyPetExp = rsp.getMyPetExp();
        this.mMyPetLevel = rsp.getMyPetLevel();
        this.mCurrentMember = rsp.getCurrentMember();
        this.mMemberLimit = rsp.getMemberLimit();
        this.mCharmLevel = rsp.getCharmLevel();
        this.mNextCharmExp = rsp.getNextCharmExp();
        this.mVipExpire = rsp.getVipExpire();
        this.mVipLevel = rsp.getVipLevel();
        this.mMedalValue = rsp.getMedalValue();
        this.mPetRanking = rsp.getPetRanking();
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
}
