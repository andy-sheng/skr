package com.wali.live.watchsdk.fans.model.task;

import com.wali.live.proto.VFansCommonProto;

/**
 * Created by zyh on 2017/11/13.
 *
 * @module 粉丝任务
 */
public class LimitGroupJobModel extends GroupJobModel {
    private String mJobInfo;
    private String mJobLimitTip;

    public LimitGroupJobModel(VFansCommonProto.LimitedGroupJobInfo info) {
        mJobType = info.getJobType().getNumber();
        mExpSum = info.getExpVal();
        mJobStatus = info.getJobStatus().getNumber();
        mJobName = info.getJobName();
        mJobInfo = info.getJobInfo();
        mJobLimitTip = "(" + info.getFinishedTimes() + "/" + info.getMaxFinishTimes() + ")";
    }

    public String getJobInfo() {
        return mJobInfo;
    }

    public String getJobLimitTip() {
        return mJobLimitTip;
    }

    @Override
    public String toString() {
        return super.toString() + "LimitGroupJobModel{" +
                "mJobInfo='" + mJobInfo + '\'' +
                ", mJobLimitTip='" + mJobLimitTip + '\'' +
                '}';
    }
}
