package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.ObjectPlayControlTemplate;
import com.common.anim.svga.SvgaParserAdapter;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.BaseRoomData;
import com.module.playways.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 灯的全屏效果
 */
public class GrabDengBigAnimationView extends RelativeLayout {
    public final String TAG = "GrabDengBigAnimationView";

    List<SVGAImageViewEx> mDengSvgaViewList = new ArrayList<>();

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
        U.getSoundUtils().preLoad(TAG, R.raw.grab_olight);
    }

    ObjectPlayControlTemplate<PlayData, SVGAImageViewEx> mViewObjectPlayControlTemplate = new ObjectPlayControlTemplate<PlayData, SVGAImageViewEx>() {
        @Override
        protected SVGAImageViewEx accept(PlayData cur) {
            return isIdle();
        }

        @Override
        public void onStart(PlayData playData, SVGAImageViewEx svgaImageView) {
            playBurstAnimationInner(playData, svgaImageView);
        }

        @Override
        protected void onEnd(PlayData playData) {

        }
    };

    public void playBurstAnimation(boolean flag) {
        MyLog.d(TAG, "playBurstAnimation");
        mViewObjectPlayControlTemplate.add(new PlayData(flag), true);
    }

    // 爆灯
    private void playBurstAnimationInner(PlayData playData, SVGAImageViewEx dengSvgaEx) {
        MyLog.d(TAG, "playBurstAnimationInner" + " playData=" + playData + " dengSvgaEx=" + dengSvgaEx);
        if (playData.isFromSelf) {
            U.getSoundUtils().play(TAG, R.raw.grab_olight);
        } else {
            //U.getSoundUtils().play(TAG, R.raw.grab_olight_lowervolume);
        }

        SVGAImageView dengSvga = dengSvgaEx.mSVGAImageView;
        if (this.indexOfChild(dengSvga) == -1) {
            MyLog.d(TAG, "视图未添加，添加");
            dengSvgaEx.add(this);
        } else {
            MyLog.d(TAG, "视图已添加");
        }
        dengSvga.setCallback(null);
        dengSvga.stopAnimation(true);
        setVisibility(VISIBLE);

        dengSvga.setVisibility(VISIBLE);
        dengSvga.setLoops(1);
        SvgaParserAdapter.parse(BaseRoomData.Companion.getGRAB_BURST_BIG_SVGA(), new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete( SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                dengSvga.setImageDrawable(drawable);
                dengSvga.startAnimation();
            }

            @Override
            public void onError() {
                MyLog.d(TAG, "playBurstAnimationInner onError");
            }
        });

        dengSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (dengSvga != null) {
                    dengSvga.setCallback(null);
                    dengSvga.stopAnimation(true);
                    setVisibility(GONE);
                }
                dengSvgaEx.playing = false;
                mViewObjectPlayControlTemplate.endCurrent(playData);
            }

            @Override
            public void onRepeat() {
                if (dengSvga != null && dengSvga.isAnimating()) {
                    dengSvga.stopAnimation(false);
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
        for (SVGAImageViewEx svgaImageViewEx : mDengSvgaViewList) {
            svgaImageViewEx.destroy();
        }
        if (mViewObjectPlayControlTemplate != null) {
            mViewObjectPlayControlTemplate.destroy();
        }
        U.getSoundUtils().release(TAG);
    }

    private SVGAImageViewEx isIdle() {
        for (SVGAImageViewEx svgaImageViewEx : mDengSvgaViewList) {
            if (!svgaImageViewEx.playing) {
                svgaImageViewEx.playing = true;
                return svgaImageViewEx;
            }
        }
        // 先让只有一个消费者
        if (mDengSvgaViewList.size() < 1) {
            SVGAImageViewEx svgaImageViewEx = new SVGAImageViewEx(new SVGAImageView(getContext()));
            mDengSvgaViewList.add(svgaImageViewEx);
            return svgaImageViewEx;
        }
        return null;
    }

    public static class PlayData {
        boolean isFromSelf;   //标记爆灯的发送者自己

        public PlayData(boolean isFlag) {
            this.isFromSelf = isFlag;
        }
    }

    public static class SVGAImageViewEx {
        public SVGAImageView mSVGAImageView;
        public boolean playing = false;

        public SVGAImageViewEx(SVGAImageView SVGAImageView) {
            mSVGAImageView = SVGAImageView;
        }

        public void add(GrabDengBigAnimationView parent) {
            LayoutParams lp = new LayoutParams(U.getDisplayUtils().dip2px(375), U.getDisplayUtils().dip2px(400));
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.topMargin = U.getDisplayUtils().dip2px(50);
            parent.addView(mSVGAImageView, lp);
        }

        public void destroy() {
            if (mSVGAImageView != null) {
                mSVGAImageView.setCallback(null);
                mSVGAImageView.stopAnimation(true);
            }
        }
    }

}
