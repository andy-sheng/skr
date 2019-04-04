package com.module.playways.rank.room.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

/**
 * 顶部动画
 */
public class RankTopLEDView extends RelativeLayout {

    public final static String TAG = "RankTopLEDView";
    public static int MID_MODE = 0;      //中间跳动一下的动画
    public static int DEFAULT_MODE = 1;  //默认模式
    public static int MIE_MODE = 2;      //灭灯模式
    public static int BAO_MODE = 3;      //爆灯模式

    SVGAImageView mDengSvga;
    int mPostion;             //view的位置，对应加载什么动画
    int curMode = DEFAULT_MODE;        //保存当前需要播什么动画, 防止动画的异步加载

    public RankTopLEDView(Context context) {
        this(context, null);
    }

    public RankTopLEDView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RankTopLEDView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.rankLED);
        mPostion = typedArray.getInt(R.styleable.rankLED_position, 0);
        typedArray.recycle();

        inflate(getContext(), R.layout.rank_top_led_view, this);
        mDengSvga = (SVGAImageView) findViewById(R.id.deng_svga);
        initSVGA();
    }

    /**
     * 中间心先跳动一下
     */
    public void playMidSVGA(SVGAListener svgaListener) {
        MyLog.d(TAG, "playMidSVGA" + " svgaListener=" + svgaListener);
        curMode = MIE_MODE;
        mDengSvga.setCallback(null);
        mDengSvga.stopAnimation(true);
        if (mPostion == 1) {
            setVisibility(VISIBLE);
            mDengSvga.setVisibility(VISIBLE);
            mDengSvga.setLoops(1);
            SVGAParser parser = new SVGAParser(U.app());
            try {
                parser.parse("rank_love_mid.svga", new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                        if (curMode == MIE_MODE) {
                            SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                            mDengSvga.setImageDrawable(drawable);
                            mDengSvga.startAnimation();
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
            } catch (Exception e) {
                MyLog.e(TAG,e);
            }


            mDengSvga.setCallback(new SVGACallback() {
                @Override
                public void onPause() {

                }

                @Override
                public void onFinished() {
                    if (mDengSvga != null) {
                        mDengSvga.stopAnimation(false);
                    }

                    if (svgaListener != null) {
                        svgaListener.onFinished();
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
        } else {
            setVisibility(GONE);
        }
    }

    // 初始状态
    public void initSVGA() {
        MyLog.d(TAG, " initSVGA " + " postion = " + mPostion);
        curMode = DEFAULT_MODE;
        mDengSvga.setCallback(null);
        mDengSvga.stopAnimation(true);

        String assetsName = "";
        switch (mPostion) {
            case 0:
                assetsName = "rank_love_left_beat.svga";
                break;
            case 1:
                assetsName = "rank_love_mid_beat.svga";
                break;
            case 2:
                assetsName = "rank_love_right_beat.svga";
                break;
        }
        setVisibility(VISIBLE);
        mDengSvga.setVisibility(VISIBLE);
        mDengSvga.setLoops(0);

        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse(assetsName, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                    if (curMode == DEFAULT_MODE) {
                        SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                        mDengSvga.setImageDrawable(drawable);
                        mDengSvga.startAnimation();
                    }
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            MyLog.e(TAG,e);
        }
    }

    // 爆灯或者灭灯
    public void setSVGAMode(boolean isBao) {
        MyLog.d(TAG, "setSVGAMode" + " isBao=" + isBao + "postion" + mPostion);
        curMode = isBao ? BAO_MODE : MIE_MODE;
        mDengSvga.setCallback(null);
        mDengSvga.stopAnimation(true);
        setVisibility(VISIBLE);
        String assetsName = isBao ? "rank_love_left.svga" : "rank_fork_left.svga";
        switch (mPostion) {
            case 0:
                assetsName = isBao ? "rank_love_left.svga" : "rank_fork_left.svga";
                break;
            case 1:
                assetsName = isBao ? "rank_love_mid.svga" : "rank_fork_mid.svga";
                break;
            case 2:
//                assetsName = isBao ? "rank_love_right.svga" : "rank_fork_right.svga";
                assetsName = isBao ? "rank_love_right.svga" : "rank_fork_end.svga";
                break;
        }
        mDengSvga.setVisibility(VISIBLE);
        mDengSvga.setLoops(1);
        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse(assetsName, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                    if (curMode == BAO_MODE || curMode == MIE_MODE) {
                        SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                        mDengSvga.setImageDrawable(drawable);
                        mDengSvga.startAnimation();
                    }
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            MyLog.e(TAG,e);
        }

        mDengSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mDengSvga != null) {
                    mDengSvga.setCallback(null);
                    mDengSvga.stopAnimation(false);
                }
                if (isBao) {
                    playBaoDengAnimation(isBao);
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

    private void playBaoDengAnimation(boolean isBAO) {
        curMode = isBAO ? BAO_MODE : DEFAULT_MODE;
        String assetsName = "rank_love_left_beat.svga";
        switch (mPostion) {
            case 0:
                assetsName = "rank_love_left_beat.svga";
                break;
            case 1:
                assetsName = "rank_love_mid_beat.svga";
                break;
            case 2:
                assetsName = "rank_love_right_beat.svga";
                break;
        }
        mDengSvga.setVisibility(VISIBLE);
        mDengSvga.setCallback(null);
        mDengSvga.setLoops(0);
        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse(assetsName, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                    if (curMode == DEFAULT_MODE || curMode == BAO_MODE) {
                        SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                        mDengSvga.setImageDrawable(drawable);
                        mDengSvga.startAnimation();
                    }
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            MyLog.e(TAG,e);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            if (mDengSvga != null) {
                mDengSvga.setCallback(null);
                mDengSvga.stopAnimation(true);
            }
        }
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
