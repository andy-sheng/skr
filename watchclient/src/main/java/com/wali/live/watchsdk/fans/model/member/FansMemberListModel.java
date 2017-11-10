package com.wali.live.watchsdk.fans.model.member;

import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2017/11/10.
 */
public class FansMemberListModel extends BaseViewModel {
    private List<FansMemberModel> mMemberList;

    public FansMemberListModel(VFansProto.MemberListRsp rsp) {
        parse(rsp);
    }

    public void parse(VFansProto.MemberListRsp rsp) {
        if (mMemberList == null) {
            mMemberList = new ArrayList();
        }
        for (VFansProto.MemberInfo protoMember : rsp.getMemListList()) {
            mMemberList.add(new FansMemberModel(protoMember));
        }
    }

    public List<FansMemberModel> getMemberList() {
        return mMemberList;
    }
}
