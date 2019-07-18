package com.module.playways.voice.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.engine.EngineEvent;
import com.engine.Params;
import com.engine.UserStatus;
import com.module.playways.R;
import com.zq.mediaengine.kit.ZqEngineKit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class VoiceRightOpView extends RelativeLayout {
    public final static String TAG = "VoiceRightOpView";
    //    Listener mListener;
    ExImageView mMicIv;
    ExTextView mVoiceTips;
    ExImageView mSpeakerIv;

    public VoiceRightOpView(Context context) {
        super(context);
        init();
    }

    public VoiceRightOpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.voice_right_op_view_layout, this);
        mMicIv = (ExImageView) this.findViewById(R.id.mic_iv);
        mVoiceTips = (ExTextView) this.findViewById(R.id.voice_tips);
        mSpeakerIv = (ExImageView) this.findViewById(R.id.speaker_iv);
        mMicIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mVoiceTips.setVisibility(GONE);
                Params params = ZqEngineKit.getInstance().getParams();
                if (params != null) {
                    if (params.isLocalAudioStreamMute()) {
                        mMicIv.setImageResource(R.drawable.jingyin_changtai);
                    } else {
                        mMicIv.setImageResource(R.drawable.jingyin_anxia);
                    }
                    ZqEngineKit.getInstance().muteLocalAudioStream(!params.isLocalAudioStreamMute());
                }
            }
        });
        mSpeakerIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                Params params = ZqEngineKit.getInstance().getParams();
                if (params != null) {
                    if (params.isAllRemoteAudioStreamsMute()) {
                        mSpeakerIv.setImageResource(R.drawable.guanbishengyin);
                        ZqEngineKit.getInstance().muteAllRemoteAudioStreams(false);
                    } else {
                        mSpeakerIv.setImageResource(R.drawable.guanbishengyin_anxia);
                        mMicIv.setImageResource(R.drawable.jingyin_anxia);
                        ZqEngineKit.getInstance().muteAllRemoteAudioStreams(true);
                        ZqEngineKit.getInstance().muteLocalAudioStream(true);
                    }
                }
            }
        });

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        Params params = ZqEngineKit.getInstance().getParams();
        if (params != null) {
            MyLog.d(TAG, "onAttachedToWindow audioMute=" + params.isLocalAudioStreamMute());
            if (params.isLocalAudioStreamMute()) {
                mMicIv.setImageResource(R.drawable.jingyin_anxia);
            } else {
                mMicIv.setImageResource(R.drawable.jingyin_changtai);
            }
            if (params.isAllRemoteAudioStreamsMute()) {
                mSpeakerIv.setImageResource(R.drawable.guanbishengyin_anxia);
            } else {
                mSpeakerIv.setImageResource(R.drawable.guanbishengyin);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent event) {

        switch (event.getType()) {
            case EngineEvent.TYPE_USER_MUTE_AUDIO: {
                //用户闭麦，开麦
                UserStatus userStatus = event.getUserStatus();
                MyLog.d(TAG, "onEvent audioMute=" + userStatus.isAudioMute());
                int userId = userStatus.getUserId();
                if (userId == MyUserInfoManager.getInstance().getUid()) {
                    if (userStatus.isAudioMute()) {
                        mMicIv.setImageResource(R.drawable.jingyin_anxia);
                    } else {
                        mMicIv.setImageResource(R.drawable.jingyin_changtai);
                    }
                }
                break;
            }
        }
    }
    //    public void setListener(Listener l) {
//        mListener = l;
//    }

//    public interface Listener {
//        void muteSelf(boolean b);
//
//        void muteOthers(boolean b);
//    }

}
