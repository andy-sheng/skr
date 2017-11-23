package com.wali.live.watchsdk.fans.holder.GroupNotify;

import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.date.DateTimeUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.ranking.holder.EmptyViewHolder;

/**
 * Created by zyh on 2017/11/23.
 */

public class BaseNotifyHolder extends EmptyViewHolder {
    protected SimpleDraweeView mAvatarDv;
    protected TextView mNameTv;
    protected TextView mApplyReasonTv;
    protected TextView mApplyTimeTv;
    protected TextView mAgreeTv;
    protected TextView mHandlerInfoTv;

    protected GroupNotifyBaseModel mGroupNotifyBaseModel;
    protected OnItemClickListener mListener;

    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public BaseNotifyHolder(View itemView) {
        super(itemView);
        mAvatarDv = $(itemView, R.id.avatar_dv);
        mNameTv = $(itemView, R.id.nickname_tv);
        mApplyReasonTv = $(itemView, R.id.apply_reason_tv);
        mApplyTimeTv = $(itemView, R.id.apply_time_tv);
        mAgreeTv = $(itemView, R.id.agree_tv);
        mHandlerInfoTv = $(itemView, R.id.handler_info_tv);
    }

    public void bindHolder(GroupNotifyBaseModel model) {
        mGroupNotifyBaseModel = model;
        mHandlerInfoTv.setVisibility(View.GONE);
        mAgreeTv.setVisibility(View.GONE);
        mApplyTimeTv.setText(DateTimeUtils.formatFeedsHumanableDate(model.getTs(),
                System.currentTimeMillis()));
    }

    protected void bindAvatar() {
        if (!TextUtils.isEmpty(mGroupNotifyBaseModel.getGroupIcon())) {
            FrescoWorker.loadImage(mAvatarDv, ImageFactory.newHttpImage(mGroupNotifyBaseModel
                    .getGroupIcon()).setIsCircle(true).build());
        } else {
            AvatarUtils.loadAvatarByUidTs(mAvatarDv, mGroupNotifyBaseModel.getGroupOwner(),
                    mGroupNotifyBaseModel.getGroupOwnerTs(), true);
        }
    }

    private final <V extends View> V $(View parent, @IdRes int resId) {
        if (parent == null) {
            return null;
        }
        return (V) parent.findViewById(resId);
    }

    public interface OnItemClickListener {
        void onAgreeJoin(GroupNotifyBaseModel model);
    }
}
