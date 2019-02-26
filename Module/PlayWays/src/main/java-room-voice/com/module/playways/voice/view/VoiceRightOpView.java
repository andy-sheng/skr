package com.module.playways.voice.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.engine.EngineManager;
import com.engine.Params;
import com.module.rank.R;

public class VoiceRightOpView extends RelativeLayout {

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
                    } else {
                        mSpeakerIv.setImageResource(R.drawable.guanbishengyin_anxia);
                    }
                    EngineManager.getInstance().muteAllRemoteAudioStreams(!params.isAllRemoteAudioStreamsMute());
                }
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Params params = EngineManager.getInstance().getParams();
        if (params != null) {
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

//    public void setListener(Listener l) {
//        mListener = l;
//    }

//    public interface Listener {
//        void muteSelf(boolean b);
//
//        void muteOthers(boolean b);
//    }

}
