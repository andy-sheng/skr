package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.module.playways.BaseRoomData;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * 灯的全屏效果
 */
public class GrabDengBigAnimationView extends RelativeLayout {

    SVGAImageView mDengSvga;

    public GrabDengBigAnimationView(Context context) {
        super(context);
        init();
    }

    public GrabDengBigAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabDengBigAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_deng_big_animation_view, this);
        mDengSvga = (SVGAImageView) findViewById(R.id.deng_svga);
    }

    // 爆灯
    public void playBurstAnimation() {
        mDengSvga.setCallback(null);
        mDengSvga.stopAnimation(true);
        setVisibility(VISIBLE);

        mDengSvga.setVisibility(VISIBLE);
        mDengSvga.setLoops(1);
        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse(new URL(BaseRoomData.GRAB_BURST_BIG_SVGA), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    mDengSvga.setImageDrawable(drawable);
                    mDengSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }

        mDengSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mDengSvga != null) {
                    mDengSvga.setCallback(null);
                    mDengSvga.stopAnimation(true);
                    setVisibility(GONE);
                }
            }

            @Override
            public void onRepeat() {
                if (mDengSvga != null && mDengSvga.isAnimating()) {
                    mDengSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDengSvga != null) {
            mDengSvga.setCallback(null);
            mDengSvga.stopAnimation(true);
        }
    }
}
