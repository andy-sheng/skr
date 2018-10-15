package com.wali.live.watchsdk.fans.holder.GroupNotify;

import android.view.View;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.model.notification.RemoveFansGroupMemNotifyModel;

/**
 * Created by zyh on 2017/11/23.
 */

public class RemoveNotifyHolder extends BaseNotifyHolder {
    public RemoveNotifyHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindHolder(GroupNotifyBaseModel model) {
        super.bindHolder(model);
        bindAvatar();
        mNameTv.setText(model.getGroupName());
        RemoveFansGroupMemNotifyModel removeModel = (RemoveFansGroupMemNotifyModel) model;
        String handlerName = removeModel.getHandler() == model.getGroupOwner() ? GlobalData.app().getResources().getString(R.string.vfans_owner) :
                removeModel.getHandler() == UserAccountManager.getInstance().getUuidAsLong() ? GlobalData.app().getResources().getString(R.string.you) :
                        GlobalData.app().getResources().getString(R.string.vfans_admin_or_deput_admin);
        String candidateName = model.getCandidate() == UserAccountManager.getInstance().getUuidAsLong() ? GlobalData.app().getString(R.string.you) :
                model.getCandidateName();
        mApplyReasonTv.setText(GlobalData.app().getString(R.string.vfans_notify_be_remove, handlerName, candidateName));
    }
}
