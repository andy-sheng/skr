package com.wali.live.watchsdk.fans.task.model;


import com.wali.live.proto.VFansCommonProto;

/**
 * Created by lan on 17-8-8.
 */
public class LimitJobModel extends GroupJobModel {
    private String mJobInfo;
    private String mJobLimitTip;

    public LimitJobModel(VFansCommonProto.LimitedGroupJobInfo protoJob) {
        parse(protoJob);
    }

    public void parse(VFansCommonProto.LimitedGroupJobInfo protoJob) {
        mJobType = protoJob.getJobType().getNumber();
        mExpSum = protoJob.getExpVal();
        mJobStatus = protoJob.getJobStatus().getNumber();
        mJobName = protoJob.getJobName();

        mJobInfo = protoJob.getJobInfo();
        mJobLimitTip = "(" + protoJob.getFinishedTimes() + "/" + protoJob.getMaxFinishTimes() + ")";
    }

    public String getJobInfo() {
        return mJobInfo;
    }

    public String getJobLimitTip() {
        return mJobLimitTip;
    }

    @Override
    protected int defaultType() {
        return TaskViewType.TYPE_GROUP_TASK;
    }

    @Override
    public String toString() {
        return "LimitJobModel{" +
                "mJobInfo='" + mJobInfo + '\'' +
                ", mJobLimitTip='" + mJobLimitTip + '\'' +
                '}';
    }
}
