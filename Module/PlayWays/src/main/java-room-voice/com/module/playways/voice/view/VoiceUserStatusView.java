package com.module.playways.voice.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.rank.R;
import com.zq.person.fragment.OtherPersonFragment;

public class VoiceUserStatusView extends RelativeLayout {

    static final int MSG_SPEAK_OVER = 1;
    BaseImageView mAvatarIv;
    ExImageView mMuteMicIv;
    ExTextView mTestTipsView;

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SPEAK_OVER: {
                    mTestTipsView.setVisibility(GONE);
                    break;
                }
            }
        }
    };

    UserInfoModel mUserInfoModel;

    public VoiceUserStatusView(Context context) {
        super(context);
        init();
    }

    public VoiceUserStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VoiceUserStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.voice_user_status_view, this);
        mAvatarIv = (BaseImageView) this.findViewById(R.id.avatar_iv);
        mMuteMicIv = (ExImageView) this.findViewById(R.id.mute_mic_iv);
        mTestTipsView = (ExTextView) this.findViewById(R.id.test_tips_view);

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(OtherPersonFragment.BUNDLE_USER_MODEL, mUserInfoModel);
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newAddParamsBuilder((FragmentActivity) getContext(), OtherPersonFragment.class)
                        .setBundle(bundle)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        });
    }

    public void bindData(UserInfoModel model) {
        mUserInfoModel = model;
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(model.getAvatar())
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColorBySex(model.getSex() == 1)
                .setCircle(true)
                .build()
        );
        mMuteMicIv.setVisibility(GONE);
        mTestTipsView.setVisibility(GONE);
    }

    public void userOffline() {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mUserInfoModel.getAvatar())
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColorBySex(mUserInfoModel.getSex() == 1)
                .setCircle(true)
                .setGray(true)
                .build()
        );
    }

    public void userMute(boolean audioMute) {
        if (audioMute) {
            mMuteMicIv.setVisibility(VISIBLE);
            mTestTipsView.setVisibility(GONE);
        } else {
            mMuteMicIv.setVisibility(GONE);
        }
    }

    public void userSpeak() {
        mTestTipsView.setVisibility(VISIBLE);
        mTestTipsView.setText("正在说话");
        mUiHanlder.removeMessages(MSG_SPEAK_OVER);
        mUiHanlder.sendEmptyMessageDelayed(MSG_SPEAK_OVER, 2000);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHanlder.removeCallbacksAndMessages(null);
    }
}
