package com.module.rankingmode.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.common.log.MyLog;
import com.common.view.ex.ExButton;
import com.common.view.ex.ExTextView;
import com.engine.EngineManager;
import com.module.rankingmode.R;
import com.xw.repo.BubbleSeekBar;

public class MixControlPanelView extends ScrollView {
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
                    float p = now / (total * 1.0f);
                    mMusicSeekbar.setProgress(p);
                    mUiHandler.sendEmptyMessageDelayed(UPDATE_MUSIC_PROGRESS, 1000);
                }
                break;
            }
        }
    };

    public MixControlPanelView(Context context) {
        super(context);
        init();
    }

    public MixControlPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        inflate(getContext(), R.layout.mix_control_panel_layout, this);

        mPlayMusicBtn = (ExButton) this.findViewById(R.id.play_music_btn);
        mMusicSeekbar = (BubbleSeekBar) this.findViewById(R.id.music_seekbar);

        if (EngineManager.getInstance().getParams().isMixMusicPlaying()) {
            mPlayMusicBtn.setText("暂停伴奏");
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
            }

        });
        mBandGainSeekbar.setProgress(EngineManager.getInstance().getParams().getBandGain());
        mBandGainSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                EngineManager.getInstance().getParams().setBandGain(progress);
            }

        });

        mPositionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton button = MixControlPanelView.this.findViewById(checkedId);
                mShowKeyTv.setText(button.getText());
                if (checkedId == R.id.mix_key1_btn) {
                    mSelectKey = 0;
                    mValueSeekbar.getConfigBuilder()
                            .max(10)
                            .min(-20)
                            .build();
                } else if (checkedId == R.id.mix_key2_btn) {
                    mSelectKey = 1;
                    mValueSeekbar.getConfigBuilder()
                            .max(10)
                            .min(-20)
                            .build();
                } else if (checkedId == R.id.mix_key3_btn) {
                    mSelectKey = 2;
                    mValueSeekbar.getConfigBuilder()
                            .max(100)
                            .min(0)
                            .build();
                } else if (checkedId == R.id.mix_key4_btn) {
                    mSelectKey = 3;
                    mValueSeekbar.getConfigBuilder()
                            .max(200)
                            .min(0)
                            .build();
                } else if (checkedId == R.id.mix_key5_btn) {
                    mSelectKey = 4;
                    mValueSeekbar.getConfigBuilder()
                            .max(100)
                            .min(0)
                            .build();
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
        if (EngineManager.getInstance().getParams().getEnableInEarMonitoring()) {
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
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHandler.removeCallbacksAndMessages(null);
    }
}
