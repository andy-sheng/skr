package com.wali.live.watchsdk.contest.model;

import com.wali.live.proto.LiveSummitProto;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestNoticeModel extends BaseViewModel {
    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_COMING = 1;
    public static final int STATUS_GOING = 2;

    private int mStatus;            //当前状态 0：普通状态 1：答题即将开始 2：答题已开始
    private float mBonus;           //奖金
    private long mStartTime;        //开始时间
    private boolean mHasInviteCode; //是否已使用复活卡

    @Deprecated
    private int mRevivalNum;        //复活卡数量，已废弃，使用GetInviteCode接口

    private float mTotalIncome;     //用户收入
    private int mRank;              //排名
    private long mZuid;
    private String mLiveId;
    private long mServerTime;       //服务器时间
    private String mVideoUrl;       //流地址

    public ContestNoticeModel(LiveSummitProto.ContestNoticeInfo protoInfo) {
        parse(protoInfo);
    }

    public void parse(LiveSummitProto.ContestNoticeInfo protoInfo) {
        mStatus = protoInfo.getStatus();
        mBonus = protoInfo.getBonus();
        mStartTime = protoInfo.getStartTime();
        mHasInviteCode = protoInfo.getHasInviteCode();

        if (protoInfo.hasRevivalNum()) {
            mRevivalNum = protoInfo.getRevivalNum();
        }

        mTotalIncome = protoInfo.getTotalIncome();
        mRank = protoInfo.getRank();
        mZuid = protoInfo.getZuid();
        mLiveId = protoInfo.getLiveid();
        mServerTime = protoInfo.getTs();
        mVideoUrl = protoInfo.getViewurl();
    }

    public int getStatus() {
        return mStatus;
    }

    public float getBonus() {
        return mBonus;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public boolean hasInviteCode() {
        return mHasInviteCode;
    }

    @Deprecated
    public int getRevivalNum() {
        return mRevivalNum;
    }

    public float getTotalIncome() {
        return mTotalIncome;
    }

    public int getRank() {
        return mRank;
    }

    public long getZuid() {
        return mZuid;
    }

    public String getLiveId() {
        return mLiveId;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    public long getDelayTime() {
        if (mServerTime == 0) {
            return 0;
        }
        return mStartTime - mServerTime;
    }
}
