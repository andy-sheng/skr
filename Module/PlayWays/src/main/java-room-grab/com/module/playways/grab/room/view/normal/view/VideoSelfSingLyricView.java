package com.module.playways.grab.room.view.normal.view;

import android.content.Context;
import android.util.AttributeSet;

import com.module.playways.R;

public class VideoSelfSingLyricView extends BaseSelfSingLyricView {
    public final static String TAG = "VideoSelfSingLyricView";

    public VideoSelfSingLyricView(Context context) {
        super(context);
    }

    public VideoSelfSingLyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoSelfSingLyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init() {
        inflate(getContext(), R.layout.video_grab_self_sing_lyric_layout, this);

        mTvLyric = findViewById(R.id.tv_lyric);
        mManyLyricsView = findViewById(R.id.many_lyrics_view);
        mVoiceScaleView = findViewById(R.id.voice_scale_view);
    }
}
