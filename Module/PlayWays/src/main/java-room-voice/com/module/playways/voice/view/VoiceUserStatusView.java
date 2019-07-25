package com.module.playways.voice.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.component.person.event.ShowPersonCardEvent;
import com.module.playways.room.room.model.RankPlayerInfoModel;
import com.module.playways.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.greenrobot.eventbus.EventBus;

public class VoiceUserStatusView extends RelativeLayout {

    public final String TAG = "VoiceUserStatusView";

    static final int MSG_SPEAK_OVER = 1;
    SVGAImageView mSpeakerSvga;
    BaseImageView mAvatarIv;
    ExImageView mMuteMicIv;
    ExImageView mLeaveIv;

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

    RankPlayerInfoModel mModel;

    boolean mMute;

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
        mLeaveIv = (ExImageView) this.findViewById(R.id.invite_tv);

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mModel != null) {
                    EventBus.getDefault().post(new ShowPersonCardEvent(mModel.getUserID()));
                }
            }
        });
    }

    public void bindData(RankPlayerInfoModel playerInfoModel) {
        mModel = playerInfoModel;
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mModel.getUserInfo().getAvatar())
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColorBySex(mModel.getUserInfo().getSex() == 1)
                .setCircle(true)
                .setGray(!mModel.isOnline())
                .build());
        if (mModel.isOnline()) {
            mLeaveIv.setVisibility(GONE);
            mMuteMicIv.setVisibility(VISIBLE);
        } else {
            mLeaveIv.setVisibility(VISIBLE);
            mMuteMicIv.setVisibility(GONE);
        }
    }

    public void userOffline() {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mModel.getUserInfo().getAvatar())
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColorBySex(mModel.getUserInfo().getSex() == 1)
                .setCircle(true)
                .setGray(true)
                .build());
        mLeaveIv.setVisibility(VISIBLE);
        mMuteMicIv.setVisibility(GONE);
    }

    public void userMute(boolean audioMute) {
        mMute = audioMute;
        if (mMute) {
            // 静音
            mMuteMicIv.setVisibility(VISIBLE);
            stopSpeakSVGA();
        } else {
            mMuteMicIv.setVisibility(GONE);
        }
    }

    public void userSpeak(int volume) {
        if (volume > 0) {
            mMuteMicIv.setVisibility(GONE);
        }
        if (volume > 30) {
            playSpeakSVGA();
        }
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mModel.getUserInfo().getAvatar())
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColorBySex(mModel.getUserInfo().getSex() == 1)
                .setCircle(true)
                .setGray(false)
                .build()
        );
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
        SvgaParserAdapter.parse( "voice_room_speak.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete( SVGAVideoEntity svgaVideoEntity) {
                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                mSpeakerSvga.setImageDrawable(drawable);
                mSpeakerSvga.startAnimation();
            }

            @Override
            public void onError() {

            }
        });
    }

    public void stopSpeakSVGA() {
        mSpeakerSvga.stopAnimation(true);
        mSpeakerSvga.setVisibility(GONE);
        if (mMute) {
            // 静音
            mMuteMicIv.setVisibility(VISIBLE);
        } else {
            mMuteMicIv.setVisibility(GONE);
        }
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
