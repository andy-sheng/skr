package com.wali.live.watchsdk.fans.holder.GroupNotify;

import android.text.TextUtils;
import android.view.View;

import com.base.global.GlobalData;
import com.base.utils.network.NetworkUtils;
import com.base.utils.toast.ToastUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.notification.ApplyJoinFansModel;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;

/**
 * Created by zyh on 2017/11/23.
 */

public class ApplyJoinNotifyHolder extends BaseNotifyHolder implements View.OnClickListener {

    public ApplyJoinNotifyHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindHolder(GroupNotifyBaseModel model) {
        super.bindHolder(model);
        ApplyJoinFansModel joinFansModel = (ApplyJoinFansModel) model;
        AvatarUtils.loadAvatarByUidTs(mAvatarDv, joinFansModel.getCandidate(), joinFansModel.getCandidateTs(), true);
        mNameTv.setText(GlobalData.app().getString(R.string.notify_apply_join_group,
                joinFansModel.getCandidateName(), joinFansModel.getGroupName()));
        if (!TextUtils.isEmpty(joinFansModel.getApplyMsg())) {
            mApplyReasonTv.setText(joinFansModel.getApplyMsg());
            mApplyReasonTv.setVisibility(View.VISIBLE);
        } else {
            mApplyReasonTv.setVisibility(View.GONE);
        }
        mAgreeTv.setVisibility(View.VISIBLE);
        mAgreeTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!NetworkUtils.hasNetwork(GlobalData.app())) {
            ToastUtils.showToast(R.string.network_disable);
            return;
        }
        if (v == mAgreeTv) {
            if (mListener != null) {
                mListener.onAgreeJoin(mGroupNotifyBaseModel);
            }
        }
    }
}