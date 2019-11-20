package com.component.notification;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.component.busilib.view.AvatarView;
import com.zq.live.proto.Common.ESex;

public class MicInviteNotifyView extends ConstraintLayout {
    ImageView mAvatarBg;
    AvatarView mAvatarIv;
    ExTextView mNameTv;
    ImageView mSexIv;
    ExTextView mHintTv;
    ExImageView mOkBtn;

    UserInfoModel mUserInfoModel;

    Listener mListener;

    public MicInviteNotifyView(Context context) {
        super(context);
        init();
    }

    public MicInviteNotifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MicInviteNotifyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.mic_invite_notification_view_layout, this);

        mAvatarBg = (ImageView) findViewById(R.id.avatar_bg);
        mAvatarIv = (AvatarView) findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) findViewById(R.id.name_tv);
        mSexIv = (ImageView) findViewById(R.id.sex_iv);
        mHintTv = (ExTextView) findViewById(R.id.hint_tv);
        mOkBtn = (ExImageView) findViewById(R.id.ok_btn);
    }

    public void bindData(UserInfoModel userInfoModel) {
        this.mUserInfoModel = userInfoModel;

        mAvatarIv.bindData(userInfoModel);
        mNameTv.setText(userInfoModel.getNickname());

        mOkBtn.setOnClickListener(new DebounceViewClickListener() {
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
