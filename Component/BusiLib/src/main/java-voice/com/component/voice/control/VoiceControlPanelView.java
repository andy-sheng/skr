package com.component.voice.control;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.engine.EngineEvent;
import com.engine.Params;
import com.zq.mediaengine.kit.ZqEngineKit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class VoiceControlPanelView extends ScrollView {
    public final String TAG = "VoiceControlPanelView";

    ExTextView mPeopleVoice;
    SeekBar mPeopleVoiceSeekbar;
    ExTextView mAccVoice;
    SeekBar mMusicVoiceSeekbar;

    RadioGroup mScenesBtnGroup;
    AppCompatRadioButton mDefaultSbtn;
    AppCompatRadioButton mKtvSbtn;
    AppCompatRadioButton mRockSbtn;
    AppCompatRadioButton mDianyinSbtn;
    AppCompatRadioButton mKonglingSbtn;

    // 记录值用来标记是否改变
    Params.AudioEffect mBeforeMode;
    int mBeforePeopleVoice;
    int mBeforeMusicVoice;

    Params.AudioEffect mAfterMode;
    int mAfterPeopleVoice;
    int mAfterMusicVoice;

    boolean isShowACC = true;  //是否显示伴奏

    public VoiceControlPanelView(Context context) {
        super(context);
        init(context, null);
    }

    public VoiceControlPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    protected int getLayout(){
        return R.layout.voice_control_panel_layout;
    }

    protected int getMarginLeft(){
        return U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(30 + 24) - U.getDisplayUtils().dip2px(53 * 5);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceControlPanelView);
        isShowACC = typedArray.getBoolean(R.styleable.VoiceControlPanelView_isShowACC, true);
        typedArray.recycle();

        inflate(getContext(), getLayout(), this);

        mPeopleVoice = (ExTextView) this.findViewById(R.id.people_voice);
        mPeopleVoiceSeekbar = (SeekBar) this.findViewById(R.id.people_voice_seekbar);
        mAccVoice = (ExTextView) this.findViewById(R.id.acc_voice);
        mMusicVoiceSeekbar = (SeekBar) this.findViewById(R.id.music_voice_seekbar);

        mDefaultSbtn = (AppCompatRadioButton) this.findViewById(R.id.default_sbtn);
        mKtvSbtn = (AppCompatRadioButton) this.findViewById(R.id.ktv_sbtn);
        mRockSbtn = (AppCompatRadioButton) this.findViewById(R.id.rock_sbtn);
        mDianyinSbtn = (AppCompatRadioButton) this.findViewById(R.id.dianyin_sbtn);
        mKonglingSbtn = (AppCompatRadioButton) this.findViewById(R.id.kongling_sbtn);

        int marginLeft = getMarginLeft();
        marginLeft = marginLeft / 6;

        if (!isShowACC) {
            mAccVoice.setVisibility(GONE);
            mMusicVoiceSeekbar.setVisibility(GONE);
        }

        setMarginLeft(mKtvSbtn, marginLeft);
        setMarginLeft(mRockSbtn, marginLeft);
        setMarginLeft(mDianyinSbtn, marginLeft);
        setMarginLeft(mKonglingSbtn, marginLeft);

//        mPeopleVoiceSeekbar.getThumb().setColorFilter(Color.parseColor("#C7C7C7"), PorterDuff.Mode.SRC_ATOP);
//        mMusicVoiceSeekbar.getThumb().setColorFilter(Color.parseColor("#C7C7C7"), PorterDuff.Mode.SRC_ATOP);

        mPeopleVoiceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                ZqEngineKit.getInstance().adjustPlaybackSignalVolume(progress);
                mAfterPeopleVoice = progress;
                ZqEngineKit.getInstance().adjustRecordingSignalVolume(progress);
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
                mAfterMusicVoice = progress;
                ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(progress);
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
                    mAfterMode = Params.AudioEffect.none;
                    ZqEngineKit.getInstance().setAudioEffectStyle(Params.AudioEffect.none);
                } else if (checkedId == R.id.ktv_sbtn) {
                    mAfterMode = Params.AudioEffect.ktv;
                    ZqEngineKit.getInstance().setAudioEffectStyle(Params.AudioEffect.ktv);
                } else if (checkedId == R.id.rock_sbtn) {
                    mAfterMode = Params.AudioEffect.rock;
                    ZqEngineKit.getInstance().setAudioEffectStyle(Params.AudioEffect.rock);
                } else if (checkedId == R.id.dianyin_sbtn) {
                    mAfterMode = Params.AudioEffect.dianyin;
                    ZqEngineKit.getInstance().setAudioEffectStyle(Params.AudioEffect.dianyin);
                } else if (checkedId == R.id.kongling_sbtn) {
                    mAfterMode = Params.AudioEffect.kongling;
                    ZqEngineKit.getInstance().setAudioEffectStyle(Params.AudioEffect.kongling);
                }
            }
        });
    }

    private void setMarginLeft(AppCompatRadioButton view, int marginLeft) {
        RadioGroup.LayoutParams layoutParams = (RadioGroup.LayoutParams) view.getLayoutParams();
        layoutParams.setMargins(marginLeft, 0, 0, 0);
    }

    public void bindData() {
        Params.AudioEffect styleEnum = null;
        if (ZqEngineKit.getInstance().getParams() != null) {
            styleEnum = ZqEngineKit.getInstance().getParams().getStyleEnum();
        }
        if (styleEnum == Params.AudioEffect.dianyin) {
            mScenesBtnGroup.check(R.id.dianyin_sbtn);
        } else if (styleEnum == Params.AudioEffect.kongling) {
            mScenesBtnGroup.check(R.id.kongling_sbtn);
        } else if (styleEnum == Params.AudioEffect.ktv) {
            mScenesBtnGroup.check(R.id.ktv_sbtn);
        } else if (styleEnum == Params.AudioEffect.rock) {
            mScenesBtnGroup.check(R.id.rock_sbtn);
        } else {
            mScenesBtnGroup.check(R.id.default_sbtn);
        }
        mPeopleVoiceSeekbar.setProgress(ZqEngineKit.getInstance().getParams().getRecordingSignalVolume());
        mMusicVoiceSeekbar.setProgress(ZqEngineKit.getInstance().getParams().getAudioMixingPlayoutVolume());

        mBeforeMode = styleEnum;
        mBeforePeopleVoice = ZqEngineKit.getInstance().getParams().getRecordingSignalVolume();
        mBeforeMusicVoice = ZqEngineKit.getInstance().getParams().getAudioMixingPlayoutVolume();

        mAfterMode = styleEnum;
        mAfterPeopleVoice = ZqEngineKit.getInstance().getParams().getRecordingSignalVolume();
        mAfterMusicVoice = ZqEngineKit.getInstance().getParams().getAudioMixingPlayoutVolume();
    }

    public boolean isChange() {
        if (mBeforeMode == mAfterMode && mBeforeMusicVoice == mAfterMusicVoice && mBeforePeopleVoice == mAfterPeopleVoice) {
            return false;
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 停止播放混音
//        ZqEngineKit.getInstance().pauseAudioMixing();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent event) {
        if (EngineEvent.TYPE_USER_SELF_JOIN_SUCCESS == event.getType()) {
            bindData();
        }
    }
}
