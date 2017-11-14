package com.wali.live.watchsdk.fans.task.model;

import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2017/11/13.
 */
public class GroupJobListModel extends BaseViewModel {
    private List<GroupJobModel> mGroupJobList;
    private List<LimitJobModel> mLimitGroupJobList;

    public GroupJobListModel(VFansProto.GroupJobListRsp rsp) {
        parse(rsp);
    }

    public void parse(VFansProto.GroupJobListRsp rsp) {
        if (mGroupJobList == null) {
            mGroupJobList = new ArrayList();
        }
        for (VFansCommonProto.GroupJobInfo protoJob : rsp.getJobListList()) {
            mGroupJobList.add(new GroupJobModel(protoJob));
        }

        if (mLimitGroupJobList == null) {
            mLimitGroupJobList = new ArrayList();
        }
        for (VFansCommonProto.LimitedGroupJobInfo protoLimitJob : rsp.getLimitedJobListList()) {
            mLimitGroupJobList.add(new LimitJobModel(protoLimitJob));
        }
    }

    public List<GroupJobModel> getGroupJobList() {
        return mGroupJobList;
    }

    public List<LimitJobModel> getLimitGroupJobList() {
        return mLimitGroupJobList;
    }
}
