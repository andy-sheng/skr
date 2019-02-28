package com.module.playways.voice.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.engine.UserStatus;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class VoiceRightOpView extends RelativeLayout {
    public final static String TAG = "VoiceRightOpView";
    //    Listener mListener;
    ExImageView mMicIv;
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
        mSpeakerIv = (ExImageView) this.findViewById(R.id.speaker_iv);
        mMicIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                Params params = EngineManager.getInstance().getParams();
                if (params != null) {
                    if (params.isLocalAudioStreamMute()) {
                        mMicIv.setImageResource(R.drawable.jingyin_changtai);
                    } else {
                        mMicIv.setImageResource(R.drawable.jingyin_anxia);
                    }
                    EngineManager.getInstance().muteLocalAudioStream(!params.isLocalAudioStreamMute());
                }
            }
        });
        mSpeakerIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                Params params = EngineManager.getInstance().getParams();
                if (params != null) {
                    if (params.isAllRemoteAudioStreamsMute()) {
                        mSpeakerIv.setImageResource(R.drawable.guanbishengyin);
                        EngineManager.getInstance().muteAllRemoteAudioStreams(false);
                    } else {
                        mSpeakerIv.setImageResource(R.drawable.guanbishengyin_anxia);
                        mMicIv.setImageResource(R.drawable.jingyin_anxia);
                        EngineManager.getInstance().muteAllRemoteAudioStreams(true);
                        EngineManager.getInstance().muteLocalAudioStream(true);
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
        Params params = EngineManager.getInstance().getParams();
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
