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
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.live.proto.Common.ESex;

/**
 * 一唱到底邀请ui
 */
public class GrabInviteNotifyView extends RelativeLayout {

    AvatarView mAvatarIv;
    ExTextView mNameTv;
    ImageView mSexIv;
    ExTextView mHintTv;
    View mAgreeTv;
    //ExTextView mIgnoreTv;

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
        mAvatarIv = this.findViewById(R.id.avatar_iv);
        mNameTv = this.findViewById(R.id.name_tv);
        mSexIv = this.findViewById(R.id.sex_iv);
        mHintTv = this.findViewById(R.id.hint_tv);
        mAgreeTv = this.findViewById(R.id.ok_btn);

        //mIgnoreTv = (ExTextView) this.findViewById(R.id.ignore_tv);

        mAgreeTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onAgree();
                }
            }
        });

//        mIgnoreTv.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mListener != null) {
//                    mListener.onIgnore();
//                }
//            }
//        });
    }

    public void bindData(UserInfoModel userInfoModel) {
        this.mUserInfoModel = userInfoModel;

        mAvatarIv.bindData(userInfoModel);
        mNameTv.setText(mUserInfoModel.getNicknameRemark());
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

    Listener mListener;

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {

        void onIgnore();

        void onAgree();
    }

}
