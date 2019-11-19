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
import com.component.busilib.view.AvatarView;
import com.component.busilib.view.NickNameView;
import com.zq.live.proto.Common.ESex;

public class DoubleInviteNotifyView extends ConstraintLayout {
    AvatarView mAvatarIv;
    NickNameView mNameView;
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

        mAvatarIv = findViewById(R.id.avatar_iv);
        mNameView = findViewById(R.id.name_view);
        mSubTitleTv = findViewById(R.id.sub_title_tv);
        mAgreeButton = findViewById(R.id.agree_button);
    }

    public void bindData(UserInfoModel userInfoModel, String extra) {
        this.mUserInfoModel = userInfoModel;

        mAvatarIv.bindData(userInfoModel);
        mNameView.setAllStateText(userInfoModel);
        mSubTitleTv.setText(extra);

        mAgreeButton.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickAgree();
                }
            }
        });
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        void onClickAgree();
    }
}
