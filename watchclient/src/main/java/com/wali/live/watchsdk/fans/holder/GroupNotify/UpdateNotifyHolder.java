package com.wali.live.watchsdk.fans.holder.GroupNotify;

import android.view.View;

import com.base.global.GlobalData;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.model.notification.UpdateFansGroupMemNotifyModel;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;

/**
 * Created by zyh on 2017/11/23.
 */

public class UpdateNotifyHolder extends BaseNotifyHolder {
    public UpdateNotifyHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindHolder(GroupNotifyBaseModel model) {
        super.bindHolder(model);
        bindAvatar();
        mNameTv.setText(model.getGroupName());
        mApplyReasonTv.setText(getContent());
    }

    private String getContent() {
        UpdateFansGroupMemNotifyModel updateModel = (UpdateFansGroupMemNotifyModel) mGroupNotifyBaseModel;
        switch (updateModel.getNotificationType()) {
            case GroupNotifyType.BE_GROUP_MANAGER_NOTIFY: {
                String handlerNotifyName = updateModel.getHandler() == updateModel.getGroupOwner()
                        ? GlobalData.app().getString(R.string.vfans_owner)
                        : GlobalData.app().getString(R.string.vfans_admin);
                String candidateNotifyName = updateModel.getCandidate() == MyUserInfoManager.getInstance().getUuid()
                        ? GlobalData.app().getString(R.string.you) : updateModel.getCandidateName();
                String roleName = updateModel.getFansGroupMemInfo().getFansGroupMemType() == VFansCommonProto.GroupMemType.ADMIN ?
                        GlobalData.app().getString(R.string.vfans_admin) : GlobalData.app().getString(R.string.vfans_deput_admin);
                return GlobalData.app().getString(R.string.vfans_notify_make_someone_to_be_admin,
                        handlerNotifyName, candidateNotifyName, roleName);
            }
            case GroupNotifyType.BE_GROUP_MEM_NOTIFY: {
                String candidateName = updateModel.getCandidate() == UserAccountManager.getInstance().getUuidAsLong()
                        ? GlobalData.app().getString(R.string.you) : updateModel.getCandidateName();
                return String.format(GlobalData.app().getResources().getString(R.string.vfans_notify_be_member), candidateName);
            }
            case GroupNotifyType.CANCEL_GROUP_MANAGER_NOTIFY: {
                String handlerNotifyName = updateModel.getHandler() == updateModel.getGroupOwner() ?
                        GlobalData.app().getString(R.string.vfans_owner) : GlobalData.app().getString(R.string.vfans_admin);
                String candidateNotifyName = updateModel.getCandidate() == UserAccountManager.getInstance().getUuidAsLong()
                        ? GlobalData.app().getResources().getString(R.string.you) : updateModel.getCandidateName();
                String roleName = GlobalData.app().getResources().getString(R.string.vfans_manager_role);
                return String.format(GlobalData.app().getString(R.string.vfans_notify_cancle_someone_to_be_admin),
                        handlerNotifyName, candidateNotifyName, roleName);
            }
        }
        return "";
    }
}