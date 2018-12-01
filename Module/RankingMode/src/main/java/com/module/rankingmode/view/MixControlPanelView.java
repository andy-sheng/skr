package com.module.rankingmode.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.view.seekbar.RotatedSeekBar;
import com.engine.EngineManager;
import com.module.rankingmode.R;

public class MixControlPanelView extends LinearLayout {
    public final static String TAG = "MixControlPanelView";


    RotatedSeekBar mVoicePitchSeekbar;


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
        mVoicePitchSeekbar = (RotatedSeekBar)this.findViewById(R.id.voice_pitch_seekbar);
        mVoicePitchSeekbar.setPercent((float) EngineManager.getInstance().getParams().getLocalVoicePitch());
        mVoicePitchSeekbar.setOnRotatedSeekBarChangeListener(new RotatedSeekBar.OnRotatedSeekBarChangeListener() {
            @Override
            public void onProgressChanged(RotatedSeekBar rotatedSeekBar, float percent, boolean fromUser) {
                MyLog.d(TAG,"appPercent  percent=" + percent + " fromUser=" + fromUser);
                EngineManager.getInstance().setLocalVoicePitch(percent);
            }

            @Override
            public void onStartTrackingTouch(RotatedSeekBar rotatedSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(RotatedSeekBar rotatedSeekBar) {

            }
        });
    }
}
