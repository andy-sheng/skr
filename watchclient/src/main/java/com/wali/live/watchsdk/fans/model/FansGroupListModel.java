package com.wali.live.watchsdk.fans.model;

import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;
import com.wali.live.watchsdk.fans.model.item.CreateFansGroupModel;
import com.wali.live.watchsdk.fans.model.item.MemFansGroupModel;
import com.wali.live.watchsdk.fans.model.item.MyFansGroupModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 17-5-24.
 */
public class FansGroupListModel extends BaseViewModel {
    private CreateFansGroupModel mCreateFansGroupModel;

    private MyFansGroupModel mMyFansGroupModel;
    private List<MemFansGroupModel> mMemFansGroupModelList;

    public FansGroupListModel(VFansProto.GetGroupListRsp rsp) {
        parse(rsp);
    }

    public void parse(VFansProto.GetGroupListRsp rsp) {
        if (rsp.hasMyGroup()) {
            mMyFansGroupModel = new MyFansGroupModel(rsp.getMyGroup());
        } else if (rsp.hasCreateRights() && rsp.getCreateRights()) {
            mCreateFansGroupModel = new CreateFansGroupModel();
        }

        if (mMemFansGroupModelList == null) {
            mMemFansGroupModelList = new ArrayList();
        }
        for (VFansProto.MemGroupInfo protoMemGroup : rsp.getGroupListList()) {
            mMemFansGroupModelList.add(new MemFansGroupModel(protoMemGroup));
        }
    }

    public boolean isFirst() {
        return mCreateFansGroupModel != null || mMyFansGroupModel != null;
    }

    public CreateFansGroupModel getCreateFansGroupModel() {
        return mCreateFansGroupModel;
    }

    public MyFansGroupModel getMyFansGroupModel() {
        return mMyFansGroupModel;
    }

    public List<MemFansGroupModel> getMemFansGroupModelList() {
        return mMemFansGroupModelList;
    }
}
