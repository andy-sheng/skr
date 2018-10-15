package com.wali.live.video.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.base.log.MyLog;
import com.live.module.common.R;

public class VideoSeekBar extends RelativeLayout {

    public final static String TAG = "VideoSeekBar";

    SeekBar mSeekBar;

    public VideoSeekBar(Context context) {
        super(context);
        init(context);
    }

    public VideoSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        inflate(context, R.layout.video_seekbar, this);
        mSeekBar = (SeekBar) this.findViewById(R.id.seek_bar);
    }

    public void setProgress(long played, long total) {
        if (played >= 0 && played <= total) {
            if (total > 0) {
                int progress = (int) (played * 100 / total);
                MyLog.d(TAG,"setProgress" + " progress=" + progress);
                mSeekBar.setProgress(progress);
            }
        }
    }

    public void setVideoSeekBarListener(SeekBar.OnSeekBarChangeListener listener) {
        mSeekBar.setOnSeekBarChangeListener(listener);
    }

}
