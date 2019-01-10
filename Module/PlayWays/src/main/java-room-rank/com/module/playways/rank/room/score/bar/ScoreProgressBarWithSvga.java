package com.module.playways.rank.room.score.bar;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;

public class ScoreProgressBarWithSvga extends RelativeLayout {

    public final static String TAG = "ScoreProgressBarWithSvga";

    ScorePrograssBar2 mScoreProgressBar;
    SVGAImageView mStarIv;

    public ScoreProgressBarWithSvga(Context context) {
        super(context);
        init();
    }

    public ScoreProgressBarWithSvga(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScoreProgressBarWithSvga(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.score_bar_wrap_layout, this);
        mScoreProgressBar = this.findViewById(R.id.score_progress_bar2);
        mStarIv = this.findViewById(R.id.star_iv);
        mStarIv.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                mStarIv.stopAnimation();
                mStarIv.setVisibility(GONE);
            }

            @Override
            public void onRepeat() {
                onFinished();
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    public void setProgress1(int progress) {
//        progress = 100;
        mScoreProgressBar.setProgress1(progress);
        int tx = mScoreProgressBar.getStarXByScore(progress);
        MyLog.d(TAG, "setProgress1" + " progress=" + progress + " star_tx=" + tx);
        if (tx > 0) {
            mStarIv.setVisibility(VISIBLE);
            mStarIv.setTranslationX(-getWidth()/2+tx);
            mStarIv.startAnimation();
        }
    }

    public void setProgress2(int progress) {
        mScoreProgressBar.setProgress2(progress);
    }
}

