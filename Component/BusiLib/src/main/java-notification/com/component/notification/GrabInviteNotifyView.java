package com.component.notification;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.component.busilib.view.AvatarView;
import com.component.busilib.view.NickNameView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.live.proto.Common.ESex;

/**
 * 一唱到底邀请ui
 */
public class GrabInviteNotifyView extends RelativeLayout {

    AvatarView mAvatarIv;
    NickNameView mNameView;
    ExTextView mHintTv;
    ExTextView mAgreeButton;

    UserInfoModel mUserInfoModel;

    public GrabInviteNotifyView(Context context) {
        super(context);
        init();
    }

    public GrabInviteNotifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabInviteNotifyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_invite_notification_view_layout, this);
        mAvatarIv = findViewById(R.id.avatar_iv);
        mNameView = findViewById(R.id.name_view);
        mHintTv = findViewById(R.id.hint_tv);
        mAgreeButton = findViewById(R.id.agree_button);

        mAgreeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onAgree();
                }
            }
        });
    }

    public void bindData(UserInfoModel userInfoModel) {
        this.mUserInfoModel = userInfoModel;

        mAvatarIv.bindData(userInfoModel);
        mNameView.setAllStateText(userInfoModel);
    }

    Listener mListener;

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {

        void onIgnore();

        void onAgree();
    }

}
