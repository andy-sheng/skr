package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.module.playways.grab.room.fragment.GrabRoomFragment;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.live.proto.Room.EQRoundOverReason;


import java.util.HashMap;

/**
 * 轮次结束
 */
public class RoundOverCardView extends RelativeLayout {

    public final static String TAG = "RoundOverCardView";

    public final static int UNKNOW_END = 0;                // 未知原因
    public final static int NONE_SING_END = -1;             // 无人想唱
    public final static int SING_PERFECT_END = 1;          // 有种优秀叫一唱到底
    public final static int SING_MOMENT_END = 2;           // 有种结束叫刚刚开始
    public final static int SING_NO_PASS_END = 3;          // 有种悲伤叫都没及格
    public final static int SING_PASS_END = 4;             // 有种遗憾叫明明可以
    public final static int SING_ENOUGH_END = 5;           // 有种可惜叫觉得你行
    public final static int SING_ABANDON_END = 6;          // 有种装x叫抢了不唱

    //0未知
    //1有种优秀叫一唱到底（全部唱完）
    //2有种结束叫刚刚开始（t<30%）
    //3有份悲伤叫都没及格(30%<=t <60%)
    //4有种遗憾叫明明可以（60%<=t<90%）
    //5有种可惜叫我觉得你行（90%<=t<=100%)

    SVGAImageView mSingResultSvga;

    SVGAListener mSVGAListener;

    public RoundOverCardView(Context context) {
        super(context);
        init();
    }

    public RoundOverCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundOverCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_round_over_card_layout, this);
        mSingResultSvga = (SVGAImageView) findViewById(R.id.sing_result_svga);
    }

    public void bindData(int songId, int reason, int resultType, SVGAListener listener) {
        this.mSVGAListener = listener;
        setVisibility(VISIBLE);
        int mode = getRoundOver(reason, resultType);
        switch (mode) {
            case NONE_SING_END: {
                LayoutParams lp = (LayoutParams) mSingResultSvga.getLayoutParams();
                lp.height = U.getDisplayUtils().dip2px(560);
                lp.topMargin = 0;
                mSingResultSvga.setLayoutParams(lp);
                startNoneSing(songId);
            }
            break;
            case SING_PERFECT_END: {
                LayoutParams lp = (LayoutParams) mSingResultSvga.getLayoutParams();
                lp.height = U.getDisplayUtils().dip2px(180);
                lp.topMargin = U.getDisplayUtils().dip2px(139);
                mSingResultSvga.setLayoutParams(lp);
                startPerfect(songId);
            }
            break;
            case SING_MOMENT_END:
            case SING_NO_PASS_END:
            case SING_PASS_END:
            case SING_ENOUGH_END:
            case SING_ABANDON_END: {
                LayoutParams lp = (LayoutParams) mSingResultSvga.getLayoutParams();
                lp.height = U.getDisplayUtils().dip2px(180);
                lp.topMargin = U.getDisplayUtils().dip2px(139);
                mSingResultSvga.setLayoutParams(lp);
                startFailed(mode, songId);
            }
            break;
            default:
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
                break;
        }
    }

    private int getRoundOver(int reason, int resultType) {
        if (reason == EQRoundOverReason.ROR_NO_ONE_SING.getValue()) {
            return NONE_SING_END;
        } else if (reason == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
            return SING_ABANDON_END;
        } else {
            // 放弃不用单独处理，看在哪个阶段点击放弃的
            return resultType;
        }
    }

    private void startNoneSing(int songId) {
//        U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_nobodywants);
        HashMap map = new HashMap();
        map.put("songId2", String.valueOf(songId));
        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                StatConstants.KEY_SONG_NO_ONE, map);
        mSingResultSvga.setVisibility(VISIBLE);
        mSingResultSvga.setLoops(1);
            SvgaParserAdapter.parse("grab_none_sing_end.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete( SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    mSingResultSvga.setImageDrawable(drawable);
                    mSingResultSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });

        mSingResultSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mSingResultSvga != null) {
                    mSingResultSvga.setCallback(null);
                    mSingResultSvga.stopAnimation(true);
                    mSingResultSvga.setVisibility(GONE);
                }

                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mSingResultSvga != null && mSingResultSvga.isAnimating()) {
                    mSingResultSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    // 优秀, 目前缺动画
    private void startPerfect(int songId) {
        U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_challengewin);
        HashMap map = new HashMap();
        map.put("song_id2", String.valueOf(songId));
        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                StatConstants.KEY_SONG_SUCCESS, map);
        mSingResultSvga.setVisibility(VISIBLE);
        mSingResultSvga.setLoops(1);
        SvgaParserAdapter.parse( "grab_sing_perfect_end.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete( SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                mSingResultSvga.setImageDrawable(drawable);
                mSingResultSvga.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mSingResultSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mSingResultSvga != null) {
                    mSingResultSvga.setCallback(null);
                    mSingResultSvga.stopAnimation(true);
                    mSingResultSvga.setVisibility(GONE);
                }

                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mSingResultSvga != null && mSingResultSvga.isAnimating()) {
                    mSingResultSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    // 不够优秀，换字即可，目前缺动画
    private void startFailed(int model, int songId) {
        U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_challengelose);
        HashMap map = new HashMap();
        map.put("song_id2", String.valueOf(songId));
        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                StatConstants.KEY_SONG_FAIL, map);
        mSingResultSvga.setVisibility(VISIBLE);
        mSingResultSvga.setLoops(1);
        String assetsName = "";
        switch (model) {
            case SING_MOMENT_END:
                assetsName = "grab_sing_moment_end.svga";
                break;
            case SING_NO_PASS_END:
                assetsName = "grab_sing_no_pass_end.svga";
                break;
            case SING_PASS_END:
                assetsName = "grab_sing_pass_end.svga";
                break;
            case SING_ENOUGH_END:
                assetsName = "grab_sing_enough_end.svga";
                break;
            case SING_ABANDON_END:
                assetsName = "grab_sing_abandon_end.svga";
                break;
        }
        SvgaParserAdapter.parse( assetsName, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete( SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                mSingResultSvga.setImageDrawable(drawable);
                mSingResultSvga.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mSingResultSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mSingResultSvga != null) {
                    mSingResultSvga.setCallback(null);
                    mSingResultSvga.stopAnimation(true);
                    mSingResultSvga.setVisibility(GONE);
                }

                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mSingResultSvga != null && mSingResultSvga.isAnimating()) {
                    mSingResultSvga.stopAnimation(false);
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
            if (mSingResultSvga != null) {
                mSingResultSvga.setCallback(null);
                mSingResultSvga.stopAnimation(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mSVGAListener = null;
        if (mSingResultSvga != null) {
            mSingResultSvga.setCallback(null);
            mSingResultSvga.stopAnimation(true);
        }
    }
}
