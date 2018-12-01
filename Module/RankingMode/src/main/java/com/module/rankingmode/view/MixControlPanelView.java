package com.module.rankingmode.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.engine.EngineManager;
import com.module.rankingmode.R;
import com.xw.repo.BubbleSeekBar;

public class MixControlPanelView extends LinearLayout {
    public final static String TAG = "MixControlPanelView";


    BubbleSeekBar mVoicePitchSeekbar;


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
        mVoicePitchSeekbar = (BubbleSeekBar) this.findViewById(R.id.voice_pitch_seekbar);
        mVoicePitchSeekbar.setProgress((float) EngineManager.getInstance().getParams().getLocalVoicePitch());
        mVoicePitchSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                EngineManager.getInstance().setLocalVoicePitch(progressFloat);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });
    }
}
