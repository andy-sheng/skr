package com.wali.live.watchsdk.fans.task.model;

import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.fans.model.type.BaseTypeModel;

/**
 * Created by lan on 2017/11/13.
 */
public class GroupJobModel extends BaseTypeModel {
    protected int mJobType;
    protected int mExpSum;
    protected int mJobStatus;
    protected String mJobName;

    protected GroupJobModel() {
    }

    public GroupJobModel(VFansCommonProto.GroupJobInfo protoJob) {
        parse(protoJob);
    }

    public void parse(VFansCommonProto.GroupJobInfo protoJob) {
        mJobType = protoJob.getJobType().getNumber();
        mExpSum = protoJob.getExpSum();
        mJobStatus = protoJob.getJobStatus().getNumber();
        mJobName = protoJob.getJobName();
    }

    public int getJobType() {
        return mJobType;
    }

    public int getExpSum() {
        return mExpSum;
    }

    public int getJobStatus() {
        return mJobStatus;
    }

    public String getJobName() {
        return mJobName;
    }

    @Override
    protected int defaultType() {
        return TaskViewType.TYPE_GROUP_TASK;
    }

    @Override
    public String toString() {
        return "GroupJobModel{" +
                "mJobType=" + mJobType +
                ", mExpSum=" + mExpSum +
                ", mJobStatus=" + mJobStatus +
                ", mJobName='" + mJobName + '\'' +
                '}';
    }
}
