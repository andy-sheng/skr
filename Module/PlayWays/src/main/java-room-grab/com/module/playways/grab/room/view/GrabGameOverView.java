package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

/**
 * 游戏结束，对战结束的动画
 */
public class GrabGameOverView extends RelativeLayout {

    public final static String TAG = "GrabGameOverView";

    SVGAImageView mEndGameIv;
    SVGAListener mSVGAListener;

    public GrabGameOverView(Context context) {
        super(context);
        init();

    }

    public GrabGameOverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabGameOverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_game_end_card_layout, this);
        mEndGameIv = (SVGAImageView) findViewById(R.id.end_game_iv);
    }

    public void starAnimation(SVGAListener listener) {
        this.mSVGAListener = listener;
        mEndGameIv.clearAnimation();
        mEndGameIv.setVisibility(VISIBLE);
        mEndGameIv.setLoops(1);
        SvgaParserAdapter.parse( "grab_game_over.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete( SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                mEndGameIv.setImageDrawable(drawable);
                mEndGameIv.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mEndGameIv.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mEndGameIv != null) {
                    mEndGameIv.setCallback(null);
                    mEndGameIv.stopAnimation(true);
                    mEndGameIv.setVisibility(GONE);
                }
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mEndGameIv != null && mEndGameIv.isAnimating()) {
                    mEndGameIv.stopAnimation(false);
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
            this.mSVGAListener = null;
            if (mEndGameIv != null) {
                mEndGameIv.setCallback(null);
                mEndGameIv.stopAnimation(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mSVGAListener = null;
        if (mEndGameIv != null) {
            mEndGameIv.setCallback(null);
            mEndGameIv.stopAnimation(true);
        }
    }
}
