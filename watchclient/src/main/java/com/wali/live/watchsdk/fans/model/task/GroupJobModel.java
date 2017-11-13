package com.wali.live.watchsdk.fans.model.task;

import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;

/**
 * Created by zyh on 2017/11/13.
 */
public class GroupJobModel extends BaseViewModel {
    protected int mJobType;
    protected int mExpSum;
    protected int mJobStatus;
    protected String mJobName;

    public GroupJobModel() {
    }

    public GroupJobModel(VFansCommonProto.GroupJobInfo info) {
        mJobType = info.getJobType().getNumber();
        mExpSum = info.getExpSum();
        mJobStatus = info.getJobStatus().getNumber();
        mJobName = info.getJobName();
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
    public String toString() {
        return "GroupJobModel{" +
                "mJobType=" + mJobType +
                ", mExpSum=" + mExpSum +
                ", mJobStatus=" + mJobStatus +
                ", mJobName='" + mJobName + '\'' +
                '}';
    }
}
