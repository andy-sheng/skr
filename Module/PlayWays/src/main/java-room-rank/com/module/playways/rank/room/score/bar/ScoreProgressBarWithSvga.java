package com.module.playways.rank.room.score.bar;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.DisplayUtils;
import com.common.utils.U;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ScoreProgressBarWithSvga extends RelativeLayout {

    public final static String TAG = "ScoreProgressBarWithSvga";

    ScorePrograssBar2 mScoreProgressBar;
    SVGAImageView mScoreAnimationIv;
    SVGAImageView mStarIv;
    SVGAParser mSVGAParser;

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
        mScoreAnimationIv = this.findViewById(R.id.score_animation_iv);
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

        mScoreAnimationIv.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                mScoreAnimationIv.stopAnimation();
                mScoreAnimationIv.setVisibility(GONE);
                mScoreProgressBar.clearHideDrawable();
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
        mScoreProgressBar.setProgress1(progress);
        int tx = mScoreProgressBar.getStarXByScore(progress);
        MyLog.d(TAG, "setProgress1" + " progress=" + progress + " star_tx=" + tx);
        if (tx > 0) {
            startStarAnimation("star.svga", tx);
            if (progress > 90) {
                startScoreAnimation("score_sss.svga", tx, R.drawable.ycjm_jdt_sss);
            } else if (progress > 75) {
                startScoreAnimation("score_ss.svga", tx, R.drawable.ycjm_jdt_ss);
            } else if (progress > 60) {
                startScoreAnimation("score_s.svga", tx, R.drawable.ycjm_jdt_s);
            } else if (progress > 45) {
                startScoreAnimation("score_a.svga", tx, R.drawable.ycjm_jdt_a);
            }
        }
    }

    public void setProgress2(int progress) {
        mScoreProgressBar.setProgress2(progress);
    }

    private void startStarAnimation(String assetsName, int tx) {
        getSVGAParser().parse(assetsName, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                mStarIv.setTranslationX(-getWidth() / 2 + tx);
                mStarIv.setVisibility(VISIBLE);
                mStarIv.stopAnimation(true);
                mStarIv.setImageDrawable(drawable);
                mStarIv.startAnimation();
            }

            @Override
            public void onError() {

            }
        });
    }

    private void startScoreAnimation(String assetsName, int tx, int drawableId) {

        getSVGAParser().parse(assetsName, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
//                LayoutParams lp = new LayoutParams(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//                mScoreAnimationIv.setLayoutParams(lp);

                mScoreAnimationIv.setTranslationX(-getWidth() / 2 + tx);
                mScoreAnimationIv.setTranslationY(-getHeight() / 2 + U.getDisplayUtils().dip2px(8));

                mScoreAnimationIv.setVisibility(VISIBLE);
                mScoreAnimationIv.stopAnimation(true);
                mScoreAnimationIv.setImageDrawable(drawable);
                mScoreAnimationIv.startAnimation();
                mScoreProgressBar.hideLevelDrawable(drawableId);
            }

            @Override
            public void onError() {

            }
        });
    }

    private SVGAParser getSVGAParser() {
        if (mSVGAParser == null) {
            mSVGAParser = new SVGAParser(getContext());
            mSVGAParser.setFileDownloader(new SVGAParser.FileDownloader() {
                @Override
                public void resume(final URL url, final Function1<? super InputStream, Unit> complete, final Function1<? super Exception, Unit> failure) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder().url(url).get().build();
                            try {
                                Response response = client.newCall(request).execute();
                                complete.invoke(response.body().byteStream());
                            } catch (IOException e) {
                                e.printStackTrace();
                                failure.invoke(e);
                            }
                        }
                    }).start();
                }
            });
        }
        return mSVGAParser;
    }
}

