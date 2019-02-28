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
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.person.fragment.OtherPersonFragment;

import org.jetbrains.annotations.NotNull;

public class VoiceUserStatusView extends RelativeLayout {

    static final int MSG_SPEAK_OVER = 1;
    SVGAImageView mSpeakerSvga;
    BaseImageView mAvatarIv;
    ExImageView mMuteMicIv;

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SPEAK_OVER: {
                    stopSpeakSVGA();
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
        mSpeakerSvga = (SVGAImageView) this.findViewById(R.id.speaker_svga);
        mAvatarIv = (BaseImageView) this.findViewById(R.id.avatar_iv);
        mMuteMicIv = (ExImageView) this.findViewById(R.id.mute_mic_iv);

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
            stopSpeakSVGA();
        } else {
            mMuteMicIv.setVisibility(GONE);
        }
    }

    public void userSpeak() {
        playSpeakSVGA();
        mUiHanlder.removeMessages(MSG_SPEAK_OVER);
        mUiHanlder.sendEmptyMessageDelayed(MSG_SPEAK_OVER, 2000);
    }

    public void playSpeakSVGA() {
        if (mSpeakerSvga.getVisibility() == VISIBLE && mSpeakerSvga.isAnimating()) {
            return;
        }
        mSpeakerSvga.stopAnimation(true);
        mSpeakerSvga.setVisibility(VISIBLE);
        mSpeakerSvga.setLoops(0);
        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse("voice_room_speak.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                    mSpeakerSvga.setImageDrawable(drawable);
                    mSpeakerSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }
    }

    public void stopSpeakSVGA() {
        mSpeakerSvga.stopAnimation(true);
        mSpeakerSvga.setVisibility(GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSpeakerSvga != null) {
            mSpeakerSvga.stopAnimation(true);
        }
        mUiHanlder.removeCallbacksAndMessages(null);
    }
}
