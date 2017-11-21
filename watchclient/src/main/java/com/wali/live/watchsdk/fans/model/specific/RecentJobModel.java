package com.wali.live.watchsdk.fans.model.specific;


import com.wali.live.proto.VFansCommonProto;

/**
 * Created by lan on 17/6/22.
 */
public class RecentJobModel {
    private long mUuid;
    private String mNickname;
    private int mGroupJobType;
    private int mJobExp;
    private long mMorningTime;

    public RecentJobModel(VFansCommonProto.RecentJobInfo protoRecentJob) {
        parse(protoRecentJob);
    }

    public void parse(VFansCommonProto.RecentJobInfo protoRecentJob) {
        this.mUuid = protoRecentJob.getUuid();
        this.mNickname = protoRecentJob.getNickname();
        this.mGroupJobType = protoRecentJob.getJobType().getNumber();
        this.mJobExp = protoRecentJob.getJobExp();
        this.mMorningTime = protoRecentJob.getMorningTime();
    }

    public long getUuid() {
        return mUuid;
    }

    public String getNickname() {
        return mNickname;
    }

    public int getGroupJobType() {
        return mGroupJobType;
    }

    public int getJobExp() {
        return mJobExp;
    }

    public long getMorningTime() {
        return mMorningTime;
    }

    @Override
    public String toString() {
        return "RecentJobModel{" +
                "mUuid=" + mUuid +
                ", mNickname='" + mNickname + '\'' +
                ", mGroupJobType=" + mGroupJobType +
                ", mJobExp=" + mJobExp +
                ", mMorningTime=" + mMorningTime +
                '}';
    }
}
