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
import com.component.busilib.view.NickNameView;
import com.zq.live.proto.Common.ESex;

public class MicInviteNotifyView extends ConstraintLayout {

    AvatarView mAvatarIv;
    NickNameView mNameView;
    ExTextView mHintTv;
    ExTextView mAgreeButton;

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

        mAvatarIv = findViewById(R.id.avatar_iv);
        mNameView = findViewById(R.id.name_view);
        mHintTv = findViewById(R.id.hint_tv);
        mAgreeButton = findViewById(R.id.agree_button);

        mAgreeButton.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickAgree();
                }
            }
        });
    }

    public void bindData(UserInfoModel userInfoModel) {
        this.mUserInfoModel = userInfoModel;

        mAvatarIv.bindData(userInfoModel);
        mNameView.setAllStateText(userInfoModel);
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        void onClickAgree();
    }
}
