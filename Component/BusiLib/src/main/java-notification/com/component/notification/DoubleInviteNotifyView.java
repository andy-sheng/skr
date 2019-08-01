package com.component.notification;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.zq.live.proto.Common.ESex;

public class DoubleInviteNotifyView extends ConstraintLayout {
    BaseImageView mAvatarIv;
    ExTextView mInviterNameTv;
    ExImageView mSexIv;
    ExTextView mSubTitleTv;
    ExTextView mAgreeButton;

    UserInfoModel mUserInfoModel;

    Listener mListener;

    public DoubleInviteNotifyView(Context context) {
        super(context);
        init();
    }

    public DoubleInviteNotifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DoubleInviteNotifyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.double_invite_notification_view_layout, this);

        mAvatarIv = (BaseImageView) findViewById(R.id.avatar_iv);
        mInviterNameTv = (ExTextView) findViewById(R.id.inviter_name_tv);
        mSexIv = (ExImageView) findViewById(R.id.sex_iv);
        mSubTitleTv = (ExTextView) findViewById(R.id.sub_title_tv);
        mAgreeButton = (ExTextView) findViewById(R.id.agree_button);
    }

    public void bindData(UserInfoModel userInfoModel, String extra) {
        this.mUserInfoModel = userInfoModel;
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                .setBorderColor(Color.WHITE)
                .build());

        mInviterNameTv.setText(userInfoModel.getNickname());
        mSubTitleTv.setText(extra);

        mAgreeButton.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickAgree();
                }
            }
        });

        if (userInfoModel.getSex() == ESex.SX_MALE.getValue()) {
            mSexIv.setVisibility(VISIBLE);
            mSexIv.setBackgroundResource(R.drawable.sex_man_icon);
        } else if (userInfoModel.getSex() == ESex.SX_FEMALE.getValue()) {
            mSexIv.setVisibility(VISIBLE);
            mSexIv.setBackgroundResource(R.drawable.sex_woman_icon);
        } else {
            mSexIv.setVisibility(GONE);
        }
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        void onClickAgree();
    }
}