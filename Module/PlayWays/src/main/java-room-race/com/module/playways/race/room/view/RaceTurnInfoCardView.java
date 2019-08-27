package com.module.playways.race.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.module.playways.R;
import com.module.playways.grab.room.listener.SVGAListener;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;


public class RaceTurnInfoCardView extends RelativeLayout {

    public final String TAG = "TurnInfoCardView";

    SVGAImageView mFirstSvga;
    SVGAImageView mNextSvga;
    SVGAImageView mWaingSvga;
    SVGAImageView mNoBodySvga;

    SVGAListener mSVGAListener;

    public RaceTurnInfoCardView(Context context) {
        super(context);
        init();
    }

    public RaceTurnInfoCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RaceTurnInfoCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.race_turn_info_card_layout, this);
        mFirstSvga = (SVGAImageView) findViewById(R.id.first_svga);
        mNextSvga = (SVGAImageView) findViewById(R.id.next_svga);
        mWaingSvga = (SVGAImageView) findViewById(R.id.waitint_svga);
        mNoBodySvga = (SVGAImageView) findViewById(R.id.nobody_svga);
    }

    // 对战开始连着第一首是同一个动画
    private void firstBegin() {
        mFirstSvga.clearAnimation();
        mFirstSvga.setVisibility(VISIBLE);
        mFirstSvga.setLoops(1);
        SvgaParserAdapter.parse("grab_battle_start.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                mFirstSvga.setImageDrawable(drawable);
                mFirstSvga.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mFirstSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mFirstSvga != null) {
                    mFirstSvga.setCallback(null);
                    mFirstSvga.stopAnimation(true);
                    mFirstSvga.setVisibility(GONE);
                }
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
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

    private void nextBegin() {
        mNextSvga.clearAnimation();
        mNextSvga.setVisibility(VISIBLE);
        mNextSvga.setLoops(1);
        SvgaParserAdapter.parse("grab_battle_next.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                mNextSvga.setImageDrawable(drawable);
                mNextSvga.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mNextSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mNextSvga != null) {
                    mNextSvga.setCallback(null);
                    mNextSvga.stopAnimation(true);
                    mNextSvga.setVisibility(GONE);
                }
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
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

    private void noBody() {
        mNoBodySvga.clearAnimation();
        mNoBodySvga.setVisibility(VISIBLE);
        mNoBodySvga.setLoops(1);
        SvgaParserAdapter.parse("grab_battle_next.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                mNoBodySvga.setImageDrawable(drawable);
                mNoBodySvga.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mNoBodySvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mNoBodySvga != null) {
                    mNoBodySvga.setCallback(null);
                    mNoBodySvga.stopAnimation(true);
                    mNoBodySvga.setVisibility(GONE);
                }
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mNoBodySvga != null && mNoBodySvga.isAnimating()) {
                    mNoBodySvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    private void waiting() {
        mWaingSvga.clearAnimation();
        mWaingSvga.setVisibility(VISIBLE);
        mWaingSvga.setLoops(1);
        SvgaParserAdapter.parse("grab_battle_next.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                mWaingSvga.setImageDrawable(drawable);
                mWaingSvga.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mWaingSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mWaingSvga != null) {
                    mWaingSvga.setCallback(null);
                    mWaingSvga.stopAnimation(true);
                    mWaingSvga.setVisibility(GONE);
                }
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mWaingSvga != null && mWaingSvga.isAnimating()) {
                    mWaingSvga.stopAnimation(false);
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
            reset();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    private void reset() {
        this.mSVGAListener = null;
        if (mFirstSvga != null) {
            mFirstSvga.setCallback(null);
            mFirstSvga.stopAnimation(true);
        }
        if (mNextSvga != null) {
            mNextSvga.setCallback(null);
            mNextSvga.stopAnimation(true);
        }
        if (mWaingSvga != null) {
            mWaingSvga.setCallback(null);
            mWaingSvga.stopAnimation(true);
        }

        if (mNoBodySvga != null) {
            mNoBodySvga.setCallback(null);
            mNoBodySvga.stopAnimation(true);
        }
    }
}
