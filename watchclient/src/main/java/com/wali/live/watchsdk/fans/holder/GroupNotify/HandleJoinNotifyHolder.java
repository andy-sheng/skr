package com.wali.live.watchsdk.fans.holder.GroupNotify;

import android.text.TextUtils;
import android.view.View;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.model.notification.HandleJoinFansGroupNotifyModel;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;

/**
 * Created by zyh on 2017/11/23.
 */

public class HandleJoinNotifyHolder  extends BaseNotifyHolder {
    public HandleJoinNotifyHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindHolder(GroupNotifyBaseModel model) {
        super.bindHolder(model);
        HandleJoinFansGroupNotifyModel handleModel = (HandleJoinFansGroupNotifyModel) model;
        if (handleModel.getId() == UserAccountManager.getInstance().getUuidAsLong()) {
            AvatarUtils.loadAvatarByUidTs(mAvatarDv, handleModel.getCandidate(), handleModel.getCandidateTs(), true);
            mNameTv.setText(GlobalData.app().getString(R.string.notify_apply_join_group,
                    handleModel.getCandidateName(), handleModel.getGroupName()));
            if (!TextUtils.isEmpty(handleModel.getMsg())) {
                mApplyReasonTv.setText(handleModel.getMsg());
                mApplyReasonTv.setVisibility(View.VISIBLE);
            } else {
                mApplyReasonTv.setVisibility(View.GONE);
            }
            if (handleModel.getNotificationType() == GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY) {
                mHandlerInfoTv.setText(R.string.apply_join_group_has_access);
            } else {
                mHandlerInfoTv.setText(R.string.apply_join_group_has_reject);
            }
            mHandlerInfoTv.setVisibility(View.VISIBLE);
        } else {
            bindAvatar();
            int handlerNameResId = handleModel.getHandler() == handleModel.getGroupOwner() ? R.string.vfans_owner :
                    handleModel.getHandler() == UserAccountManager.getInstance().getUuidAsLong() ?
                            R.string.you : R.string.vfans_admin_or_deput_admin;
            String handlerName = GlobalData.app().getString(handlerNameResId);
            String candidateName = model.getCandidate() == UserAccountManager.getInstance().getUuidAsLong()
                    ? GlobalData.app().getResources().getString(R.string.you) : model.getCandidateName();
            if (handleModel.getNotificationType() == GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY) {
                mApplyReasonTv.setText(GlobalData.app().getString(R.string.vfans_group_someone_agree_join, handlerName, candidateName));
            } else {
                mHandlerInfoTv.setText(GlobalData.app().getString(R.string.vfans_group_someone_reject_join, handlerName, candidateName));
            }
        }
    }
}
