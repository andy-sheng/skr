package com.module.rankingmode.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.common.log.MyLog;
import com.common.view.ex.ExButton;
import com.common.view.ex.ExTextView;
import com.engine.EngineManager;
import com.engine.agora.effect.EffectModel;
import com.module.rankingmode.R;
import com.xw.repo.BubbleSeekBar;

public class AudioControlPanelView extends ScrollView {
    public final static String TAG = "MixControlPanelView";
    public final static int UPDATE_MUSIC_PROGRESS = 100;

    ExButton mPlayMusicBtn;
    BubbleSeekBar mMusicSeekbar;
    BubbleSeekBar mMusicVolumeSeekbar;

    BubbleSeekBar mVoicePitchSeekbar;
    BubbleSeekBar mBandFeqSeekbar;
    BubbleSeekBar mBandGainSeekbar;
    RadioGroup mPositionRadioGroup;
    ExTextView mShowKeyTv;
    BubbleSeekBar mValueSeekbar;

    CheckBox mEarOpenCb;
    BubbleSeekBar mEarVolumeSeekbar;

    CheckBox mMuteAllRemoteAudio;
    CheckBox mMuteSelfAudio;

    private int mSelectKey = 0;

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_MUSIC_PROGRESS: {
                    int now = EngineManager.getInstance().getAudioMixingCurrentPosition();
                    int total = EngineManager.getInstance().getAudioMixingDuration();
                    MyLog.d(TAG, "now:" + now + " totoal:" + total);
                    float p = now*100 / total;
                    mMusicSeekbar.setProgress(p);
                    mUiHandler.sendEmptyMessageDelayed(UPDATE_MUSIC_PROGRESS, 1000);
                }
                break;
            }
        }
    };

    public AudioControlPanelView(Context context) {
        super(context);
        init();
    }

    public AudioControlPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private ViewTreeObserver.OnScrollChangedListener scrollListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            correct(AudioControlPanelView.this);
        }
    };

    void correct(View view) {
        // 矫正气泡
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                correct(child);
            }
        } else if (view instanceof BubbleSeekBar) {
            ((BubbleSeekBar) view).correctOffsetWhenContainerOnScrolling();
        }
    }

    void init() {
        inflate(getContext(), R.layout.audio_control_panel_layout, this);

        this.getViewTreeObserver().addOnScrollChangedListener(scrollListener);
        mPlayMusicBtn = (ExButton) this.findViewById(R.id.play_music_btn);
        mMusicSeekbar = (BubbleSeekBar) this.findViewById(R.id.music_seekbar);

        if (EngineManager.getInstance().getParams().isMixMusicPlaying()) {
            mPlayMusicBtn.setText("暂停伴奏");
            mUiHandler.removeMessages(UPDATE_MUSIC_PROGRESS);
            mUiHandler.sendEmptyMessage(UPDATE_MUSIC_PROGRESS);
        } else {
            mPlayMusicBtn.setText("播放伴奏");
        }
        mMusicSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                int total = EngineManager.getInstance().getAudioMixingDuration();
                EngineManager.getInstance().setAudioMixingPosition((int) (progressFloat * total / 100));
            }

            @Override
            public void onTouchEvent(BubbleSeekBar bubbleSeekBar, int action) {
                super.onTouchEvent(bubbleSeekBar, action);
                if (action == MotionEvent.ACTION_DOWN) {
                    mUiHandler.removeMessages(UPDATE_MUSIC_PROGRESS);
                } else if (action == MotionEvent.ACTION_UP) {
                    if (EngineManager.getInstance().getParams().isMixMusicPlaying()) {
                        mUiHandler.removeMessages(UPDATE_MUSIC_PROGRESS);
                        mUiHandler.sendEmptyMessage(UPDATE_MUSIC_PROGRESS);
                    }
                }
            }
        });
        // 这个点击事件 被 Dialog 框架吃了 有可能
        mPlayMusicBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EngineManager.getInstance().getParams().isMixMusicPlaying()) {
                    EngineManager.getInstance().stopAudioMixing();
                    mUiHandler.removeMessages(UPDATE_MUSIC_PROGRESS);
                    mPlayMusicBtn.setText("播放伴奏");
                } else {
                    // 不再播放
                    EngineManager.getInstance().startAudioMixing("/assets/test.mp3", false, false, 1);
                    mUiHandler.removeMessages(UPDATE_MUSIC_PROGRESS);
                    mUiHandler.sendEmptyMessage(UPDATE_MUSIC_PROGRESS);
                    mPlayMusicBtn.setText("暂停伴奏");
                }
            }
        });

        mMusicVolumeSeekbar = this.findViewById(R.id.music_volume_seekbar);
        mMusicVolumeSeekbar.setProgress(EngineManager.getInstance().getParams().getAudioMixingVolume());
        mMusicVolumeSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                EngineManager.getInstance().adjustAudioMixingVolume(progress);
            }
        });

        findViewById(R.id.play_effect_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                EffectModel effectModel = EngineManager.getInstance().getAllEffects().get(0);
                EngineManager.getInstance().playEffects(effectModel);
            }
        });

        mVoicePitchSeekbar = (BubbleSeekBar) this.findViewById(R.id.voice_pitch_seekbar);
        mBandFeqSeekbar = (BubbleSeekBar) this.findViewById(R.id.band_feq_seekbar);
        mBandGainSeekbar = (BubbleSeekBar) this.findViewById(R.id.band_gain_seekbar);
        mPositionRadioGroup = (RadioGroup) this.findViewById(R.id.positionRadioGroup);
        mShowKeyTv = (ExTextView) this.findViewById(R.id.show_key_tv);
        mValueSeekbar = (BubbleSeekBar) this.findViewById(R.id.value_seekbar);

        mVoicePitchSeekbar.setProgress((float) EngineManager.getInstance().getParams().getLocalVoicePitch());
        mVoicePitchSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                EngineManager.getInstance().setLocalVoicePitch(progressFloat);
            }

        });

        mBandFeqSeekbar.setProgress(EngineManager.getInstance().getParams().getBandFrequency());
        mBandFeqSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                EngineManager.getInstance().getParams().setBandFrequency(progress);
                EngineManager.getInstance().setLocalVoiceEqualization();
            }

        });
        mBandGainSeekbar.setProgress(EngineManager.getInstance().getParams().getBandGain());
        mBandGainSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                EngineManager.getInstance().getParams().setBandGain(progress);
                EngineManager.getInstance().setLocalVoiceEqualization();
            }

        });

        mPositionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton button = AudioControlPanelView.this.findViewById(checkedId);
                mShowKeyTv.setText(button.getText());
                if (checkedId == R.id.mix_key1_btn) {
                    mSelectKey = 0;
                    mValueSeekbar.getConfigBuilder()
                            .max(10)
                            .min(-20)
                            .build();
                    mValueSeekbar.setProgress(EngineManager.getInstance().getParams().getLocalVoiceReverb(0));
                } else if (checkedId == R.id.mix_key2_btn) {
                    mSelectKey = 1;
                    mValueSeekbar.getConfigBuilder()
                            .max(10)
                            .min(-20)
                            .build();
                    mValueSeekbar.setProgress(EngineManager.getInstance().getParams().getLocalVoiceReverb(1));
                } else if (checkedId == R.id.mix_key3_btn) {
                    mSelectKey = 2;
                    mValueSeekbar.getConfigBuilder()
                            .max(100)
                            .min(0)
                            .build();
                    mValueSeekbar.setProgress(EngineManager.getInstance().getParams().getLocalVoiceReverb(2));
                } else if (checkedId == R.id.mix_key4_btn) {
                    mSelectKey = 3;
                    mValueSeekbar.getConfigBuilder()
                            .max(200)
                            .min(0)
                            .build();
                    mValueSeekbar.setProgress(EngineManager.getInstance().getParams().getLocalVoiceReverb(3));
                } else if (checkedId == R.id.mix_key5_btn) {
                    mSelectKey = 4;
                    mValueSeekbar.getConfigBuilder()
                            .max(100)
                            .min(0)
                            .build();
                    mValueSeekbar.setProgress(EngineManager.getInstance().getParams().getLocalVoiceReverb(4));
                }
            }
        });

        mValueSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                EngineManager.getInstance().setLocalVoiceReverb(mSelectKey, progress);
            }
        });

        mEarOpenCb = (CheckBox) this.findViewById(R.id.ear_open_cb);
        mEarVolumeSeekbar = (BubbleSeekBar) this.findViewById(R.id.ear_volume_seekbar);
        if (EngineManager.getInstance().getParams().isEnableInEarMonitoring()) {
            mEarOpenCb.setChecked(true);
        } else {
            mEarOpenCb.setChecked(false);
        }
        mEarOpenCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EngineManager.getInstance().enableInEarMonitoring(isChecked);
            }
        });
        mEarVolumeSeekbar.setProgress(EngineManager.getInstance().getParams().getInEarMonitoringVolume());
        mEarVolumeSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                EngineManager.getInstance().setInEarMonitoringVolume(progress);
            }
        });

        mMuteAllRemoteAudio = this.findViewById(R.id.mute_all_remote_audio);
        mMuteAllRemoteAudio.setChecked(EngineManager.getInstance().getParams().isAllRemoteAudioStreamsMute());
        mMuteAllRemoteAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                EngineManager.getInstance().muteAllRemoteAudioStreams(b);
            }
        });

        mMuteSelfAudio = this.findViewById(R.id.mute_self_audio);
        mMuteSelfAudio.setChecked(EngineManager.getInstance().getParams().isLocalAudioStreamMute());
        mMuteSelfAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                EngineManager.getInstance().muteLocalAudioStream(b);
            }
        });
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHandler.removeCallbacksAndMessages(null);
        this.getViewTreeObserver().removeOnScrollChangedListener(scrollListener);
    }
}
