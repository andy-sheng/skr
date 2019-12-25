package com.component.notification;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.component.busilib.view.AvatarView;
import com.component.busilib.view.NickNameView;

public class RelayInviteNotifyView extends ConstraintLayout {

    AvatarView mAvatarIv;
    NickNameView mNameView;
    ExTextView mHintTv;
    ExTextView mAgreeButton;

    UserInfoModel mUserInfoModel;

    Listener mListener;

    public RelayInviteNotifyView(Context context) {
        super(context);
        init();
    }

    public RelayInviteNotifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RelayInviteNotifyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.normal_invite_notification_view_layout, this);

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

    public void bindData(UserInfoModel userInfoModel,String  text) {
        this.mUserInfoModel = userInfoModel;
        mHintTv.setText(text);
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
