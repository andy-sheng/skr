package com.wali.live.watchsdk.personalcenter.level.model;

import android.support.annotation.NonNull;

import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.Vip.VipProto;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述用户VIP等级的数据结构<br>
 * Scope:界面<br>
 * Created by rongzhisheng on 17-4-21.
 * @module 等级
 */
public class UserVipInfo {

    public static UserVipInfo newInstance(@NonNull VipProto.VipHomePageRsp rsp) {
        return new UserVipInfo(rsp);
    }

    private UserVipInfo(@NonNull VipProto.VipHomePageRsp rsp) {
        if (rsp.getRet() != ErrorCode.CODE_SUCCESS) {
            return;
        }
        mLevel = rsp.getVipLevel();
        mExp = rsp.getVipExp();
        mUpdateRequiredExp = rsp.getNextVipLevelExp();
        mNextLevelExpGap = rsp.getVipLevelInterval();
        mLevelName = rsp.getVipLevelName();
        mVipPrivilegeList = new ArrayList<>();
        if (rsp.getVipPrivilegesList() != null && !rsp.getVipPrivilegesList().isEmpty()) {
            for (VipProto.VipPrivilege p : rsp.getVipPrivilegesList()) {
                if (p == null) {
                    continue;
                }
                mVipPrivilegeList.add(new VipPrivilege(p));
            }
        }
        mIsFrozen = rsp.getVipDisable();
        mTotalSentGoldGem = rsp.getTotalGemCnt();
        mUpdateRequiredGem = rsp.getNeedGemCnt();
    }

    //TODO 以下是字段声明和getter和setter

    /**vip等级*/
    private int mLevel;
    /**vip经验值*/
    private int mExp;
    /**用户升下一级vip所需经验值，达到满级时为0*/
    private int mUpdateRequiredExp;
    /**当前等级升下一级需要的经验值，达到满级时为0*/
    private int mNextLevelExpGap;
    /**vip等级名称*/
    private String mLevelName;
    /**vip是否被冻结*/
    private boolean mIsFrozen;
    /**特权列表*/
    private List<VipPrivilege> mVipPrivilegeList;
    /**送出金钻总数*/
    private int mTotalSentGoldGem;
    /**升级所需送出金钻数*/
    private int mUpdateRequiredGem;

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public int getExp() {
        return mExp;
    }

    /**
     * @return 当前经验距离下一等需要级经验值. 满级时为0
     */
    public int getUpdateRequiredExp() {
        return mUpdateRequiredExp;
    }

    public int getNextLevelExpGap() {
        return mNextLevelExpGap;
    }

    public String getLevelName() {
        return mLevelName;
    }

    public boolean isFrozen() {
        return mIsFrozen;
    }

    public int getTotalSentGoldGem() {
        return mTotalSentGoldGem;
    }

    public int getUpdateRequiredGem() {
        return mUpdateRequiredGem;
    }

    public boolean isMaxLevel() {
        return mUpdateRequiredExp == 0;
    }

    /**
     * @return 是否获得过隐身特权，不考虑冻结状态
     */
    public boolean canHide() {
        if (mVipPrivilegeList == null || mVipPrivilegeList.isEmpty()) {
            return false;
        }
        for (VipPrivilege p : mVipPrivilegeList) {
            if (p.getType() != VipPrivilege.TYPE_HIDE) {
                continue;
            }
            return p.isGained();// 是否获得隐身特权
        }
        return false;// 这个权限不再支持了
    }

    public List<VipPrivilege> getVipPrivilegeList() {
        return mVipPrivilegeList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserVipInfo{");
        sb.append("mLevel=").append(mLevel);
        sb.append(", mExp=").append(mExp);
        sb.append(", mUpdateRequiredExp=").append(mUpdateRequiredExp);
        sb.append(", mNextLevelExpGap=").append(mNextLevelExpGap);
        sb.append(", mLevelName='").append(mLevelName).append('\'');
        sb.append(", mIsFrozen=").append(mIsFrozen);
        sb.append(", mVipPrivilegeList=").append(mVipPrivilegeList);
        sb.append(", mTotalSentGoldGem=").append(mTotalSentGoldGem);
        sb.append(", mUpdateRequiredGem=").append(mUpdateRequiredGem);
        sb.append('}');
        return sb.toString();
    }

    /**
     * vip特权
     */
    public static class VipPrivilege {
        private static final int TYPE_HIDE = 6; // 隐身
        public static final int TYPE_MORE = 0;  // 敬请期待

        private VipPrivilege(@NonNull VipProto.VipPrivilege p) {
            mName = p.getName();
            mType = p.getType();
            mUnlockLevel = p.getOpenLevel();
            mGained = p.getGained();
        }

        public VipPrivilege(String name, int type, int unlockLevel, boolean gained) {
            mName = name;
            mType = type;
            mUnlockLevel = unlockLevel;
            mGained = gained;
        }

        private String mName;//特权名称
        private int mType;//或者叫id，用数字唯一标识的特权类型. 1:徽章, 2:飘屏, 3:闪耀进场, 4:至尊进场, 5:终极进场, 6: 隐身, 7: 专享充值
        private int mUnlockLevel;//该特权开启的最低等级要求
        private boolean mGained;//用户是否获得该项vip特权

        public String getName() {
            return mName;
        }

        public int getType() {
            return mType;
        }

        public int getUnlockLevel() {
            return mUnlockLevel;
        }

        public boolean isGained() {
            return mGained;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("VipPrivilege{");
            sb.append("mName='").append(mName).append('\'');
            sb.append(", mType=").append(mType);
            sb.append(", mUnlockLevel=").append(mUnlockLevel);
            sb.append(", mGained=").append(mGained);
            sb.append('}');
            return sb.toString();
        }
    }

}
