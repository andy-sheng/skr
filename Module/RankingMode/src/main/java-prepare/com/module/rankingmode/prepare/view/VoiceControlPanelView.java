package com.module.rankingmode.prepare.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;

import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.engine.EngineManager;
import com.engine.Params;
import com.module.rankingmode.R;

public class VoiceControlPanelView extends ScrollView {
    public final static String TAG = "VoiceControlPanelView";
    public final static int UPDATE_MUSIC_PROGRESS = 100;

    SeekBar mPeopleVoiceSeekbar;
    SeekBar mMusicVoiceSeekbar;
    RadioGroup mScenesBtnGroup;


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

        mPeopleVoiceSeekbar.getThumb().setColorFilter(Color.parseColor("#C7C7C7"), PorterDuff.Mode.SRC_ATOP);
        mMusicVoiceSeekbar.getThumb().setColorFilter(Color.parseColor("#C7C7C7"), PorterDuff.Mode.SRC_ATOP);

        mScenesBtnGroup = (RadioGroup) this.findViewById(R.id.scenes_btn_group);
        mScenesBtnGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                MyLog.d(TAG, "onCheckedChanged" + " group=" + group + " checkedId=" + checkedId);

            }
        });
        mScenesBtnGroup.check(R.id.default_sbtn);

        if (!EngineManager.getInstance().isInit()) {
            // 不能每次都初始化,播放伴奏
            EngineManager.getInstance().init(Params.newBuilder(Params.CHANNEL_TYPE_COMMUNICATION)
                    .setEnableVideo(false)
                    .build());
            EngineManager.getInstance().joinRoom("" + System.currentTimeMillis(), (int) UserAccountManager.getInstance().getUuidAsLong(), true);
            EngineManager.getInstance().startAudioMixing("/assets/test.mp3", true, false, -1);
        } else {
            EngineManager.getInstance().resumeAudioMixing();
        }
        mMusicVoiceSeekbar.setProgress(EngineManager.getInstance().getParams().getAudioMixingVolume());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 停止播放混音
        EngineManager.getInstance().pauseAudioMixing();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}
