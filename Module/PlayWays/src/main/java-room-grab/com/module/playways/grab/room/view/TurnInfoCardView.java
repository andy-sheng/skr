package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

public class TurnInfoCardView extends RelativeLayout {

    public final static String TAG = "TurnInfoCardView";

    public static final int MODE_SONG_SEQ = 1;
    public static final int MODE_BATTLE_BEGIN = 2;

    SVGAImageView mFirstSvga;
    SVGAImageView mNextSvga;

    int mMode = MODE_BATTLE_BEGIN;

    public TurnInfoCardView(Context context) {
        super(context);
        init();
    }

    public TurnInfoCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TurnInfoCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_turn_info_card_layout, this);
        mFirstSvga = (SVGAImageView) findViewById(R.id.first_svga);
        mNextSvga = (SVGAImageView) findViewById(R.id.next_svga);
    }

    public void setModeSongSeq(boolean first, SVGAListener listener) {
        MyLog.d(TAG, "setModeSongSeq" + " first=" + first + " listener=" + listener);
        setVisibility(VISIBLE);
        if (first) {
            firstBegin(listener);
        } else {
            nextBegin(listener);
        }
    }

    // 对战开始连着第一首是同一个动画
    private void firstBegin(SVGAListener listener) {
        mFirstSvga.setVisibility(VISIBLE);
        mFirstSvga.setLoops(1);
        SVGAParser parser = new SVGAParser(getContext());
        try {
            parser.parse("battle_start.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    mFirstSvga.setImageDrawable(drawable);
                    mFirstSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }

        mFirstSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mFirstSvga != null) {
                    mFirstSvga.stopAnimation(true);
                }
                if (listener != null) {
                    listener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mFirstSvga != null && mFirstSvga.isAnimating()) {
                    mFirstSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    private void nextBegin(SVGAListener listener) {
        mNextSvga.setVisibility(VISIBLE);
        mNextSvga.setLoops(1);
        SVGAParser parser = new SVGAParser(getContext());
        try {
            parser.parse("battle_start.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    mNextSvga.setImageDrawable(drawable);
                    mNextSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }

        mNextSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mNextSvga != null) {
                    mNextSvga.stopAnimation(true);
                }
                if (listener != null) {
                    listener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mNextSvga != null && mNextSvga.isAnimating()) {
                    mNextSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            if (mFirstSvga != null) {
                mFirstSvga.stopAnimation(false);
            }

            if (mNextSvga != null) {
                mNextSvga.stopAnimation(false);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mFirstSvga != null) {
            mFirstSvga.stopAnimation(true);
        }
        if (mNextSvga != null) {
            mNextSvga.stopAnimation(true);
        }
    }
}
