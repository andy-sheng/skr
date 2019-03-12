package com.module.playways.rank.prepare.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;

import com.common.log.MyLog;
import com.common.utils.U;
import com.engine.EngineManager;
import com.engine.Params;
import com.module.rank.R;

public class VoiceControlPanelView extends ScrollView {
    public final static String TAG = "VoiceControlPanelView";

    SeekBar mPeopleVoiceSeekbar;
    SeekBar mMusicVoiceSeekbar;
    RadioGroup mScenesBtnGroup;
    ScenesSelectBtn mDefaultSbtn;
    ScenesSelectBtn mKtvSbtn;
    ScenesSelectBtn mRockSbtn;
    ScenesSelectBtn mDianyinSbtn;
    ScenesSelectBtn mKonglingSbtn;

    // 记录值用来标记是否改变
    Params.AudioEffect beforeMode;
    int beforePeopleVoice;
    int beforeMusicVoice;

    Params.AudioEffect afterMode;
    int afterPeopleVoice;
    int afterMusicVoice;

    public VoiceControlPanelView(Context context) {
        super(context);
        init();
    }

    public VoiceControlPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        inflate(getContext(), R.layout.voice_control_panel_layout, this);

        mPeopleVoiceSeekbar = (SeekBar) this.findViewById(R.id.people_voice_seekbar);
        mMusicVoiceSeekbar = (SeekBar) this.findViewById(R.id.music_voice_seekbar);

        mDefaultSbtn = (ScenesSelectBtn) this.findViewById(R.id.default_sbtn);
        mKtvSbtn = (ScenesSelectBtn) this.findViewById(R.id.ktv_sbtn);
        mRockSbtn = (ScenesSelectBtn) this.findViewById(R.id.rock_sbtn);
        mDianyinSbtn = (ScenesSelectBtn) this.findViewById(R.id.dianyin_sbtn);
        mKonglingSbtn = (ScenesSelectBtn) this.findViewById(R.id.kongling_sbtn);

        int marginLeft = U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(30 + 24) - U.getDisplayUtils().dip2px(53 * 5);
        marginLeft = marginLeft / 6;

        setMarginLeft(mKtvSbtn, marginLeft);
        setMarginLeft(mRockSbtn, marginLeft);
        setMarginLeft(mDianyinSbtn, marginLeft);
        setMarginLeft(mKonglingSbtn, marginLeft);

//        mPeopleVoiceSeekbar.getThumb().setColorFilter(Color.parseColor("#C7C7C7"), PorterDuff.Mode.SRC_ATOP);
//        mMusicVoiceSeekbar.getThumb().setColorFilter(Color.parseColor("#C7C7C7"), PorterDuff.Mode.SRC_ATOP);

        mPeopleVoiceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                EngineManager.getInstance().adjustPlaybackSignalVolume(progress);
                afterPeopleVoice = progress;
                EngineManager.getInstance().adjustRecordingSignalVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mMusicVoiceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                afterMusicVoice = progress;
                EngineManager.getInstance().adjustAudioMixingVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mScenesBtnGroup = (RadioGroup) this.findViewById(R.id.scenes_btn_group);

        mScenesBtnGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                MyLog.d(TAG, "onCheckedChanged" + " group=" + group + " checkedId=" + checkedId);
                if (checkedId == R.id.default_sbtn) {
                    afterMode = Params.AudioEffect.none;
                    EngineManager.getInstance().setAudioEffectStyle(Params.AudioEffect.none);
                } else if (checkedId == R.id.dianyin_sbtn) {
                    afterMode = Params.AudioEffect.tb1;
                    EngineManager.getInstance().setAudioEffectStyle(Params.AudioEffect.tb1);
                } else if (checkedId == R.id.kongling_sbtn) {
                    afterMode = Params.AudioEffect.tb2;
                    EngineManager.getInstance().setAudioEffectStyle(Params.AudioEffect.tb2);
                } else if (checkedId == R.id.ktv_sbtn) {
                    afterMode = Params.AudioEffect.cb1;
                    EngineManager.getInstance().setAudioEffectStyle(Params.AudioEffect.cb1);
                } else if (checkedId == R.id.rock_sbtn) {
                    afterMode = Params.AudioEffect.cb2;
                    EngineManager.getInstance().setAudioEffectStyle(Params.AudioEffect.cb2);
                }
            }
        });
    }

    private void setMarginLeft(ScenesSelectBtn scenesSelectBtn, int marginLeft) {
        RadioGroup.LayoutParams layoutParams = (RadioGroup.LayoutParams) scenesSelectBtn.getLayoutParams();
        layoutParams.setMargins(marginLeft, 0, 0, 0);
    }

    public void bindData() {
        Params.AudioEffect styleEnum = null;
        if (EngineManager.getInstance().getParams() != null) {
            styleEnum = EngineManager.getInstance().getParams().getStyleEnum();
        }

        if (styleEnum == Params.AudioEffect.tb1) {
            mScenesBtnGroup.check(R.id.dianyin_sbtn);
        } else if (styleEnum == Params.AudioEffect.tb2) {
            mScenesBtnGroup.check(R.id.kongling_sbtn);
        } else if (styleEnum == Params.AudioEffect.cb1) {
            mScenesBtnGroup.check(R.id.ktv_sbtn);
        } else if (styleEnum == Params.AudioEffect.cb2) {
            mScenesBtnGroup.check(R.id.rock_sbtn);
        } else {
            mScenesBtnGroup.check(R.id.default_sbtn);
        }
        mPeopleVoiceSeekbar.setProgress(EngineManager.getInstance().getParams().getRecordingSignalVolume());
        mMusicVoiceSeekbar.setProgress(EngineManager.getInstance().getParams().getAudioMixingVolume());

        beforeMode = styleEnum;
        beforePeopleVoice = EngineManager.getInstance().getParams().getRecordingSignalVolume();
        beforeMusicVoice = EngineManager.getInstance().getParams().getAudioMixingVolume();

        afterMode = styleEnum;
        afterPeopleVoice = EngineManager.getInstance().getParams().getRecordingSignalVolume();
        afterMusicVoice = EngineManager.getInstance().getParams().getAudioMixingVolume();
    }

    public boolean isChange() {
        if (beforeMode == afterMode && beforeMusicVoice == afterMusicVoice && beforePeopleVoice == afterPeopleVoice) {
            return false;
        }

        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 停止播放混音
//        EngineManager.getInstance().pauseAudioMixing();

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}
