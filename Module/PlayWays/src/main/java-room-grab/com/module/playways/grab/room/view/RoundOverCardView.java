package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;

/**
 * 轮次结束
 */
public class RoundOverCardView extends RelativeLayout {

    public final static int NONE_SING_END = 1;             // 无人想唱
    public final static int SING_PERFECT_END = 2;          // 有种优秀叫一唱到底
    public final static int SING_MOMENT_END = 3;           // 有种结束叫刚刚开始
    public final static int SING_NO_PASS_END = 4;          // 有种悲伤叫都没及格
    public final static int SING_PASS_END = 5;             // 有种遗憾叫明明可以
    public final static int SING_ENOUGH_END = 6;           // 有种可惜叫觉得你行

    SVGAImageView mNoneSingSvga;

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
        mNoneSingSvga = (SVGAImageView) findViewById(R.id.none_sing_svga);
    }

    public void bindData(int mode, SVGAListener listener) {
        switch (mode) {
            case NONE_SING_END:
                startNoneSing(listener);
                break;
            case SING_PERFECT_END:
                startPerfect(listener);
                break;
            case SING_MOMENT_END:
            case SING_NO_PASS_END:
            case SING_PASS_END:
            case SING_ENOUGH_END:
                startNotPerfect(listener);
                break;
            default:
                break;
        }
    }

    private void startNoneSing(SVGAListener listener) {
        mNoneSingSvga.setVisibility(VISIBLE);
        mNoneSingSvga.startAnimation();

        mNoneSingSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mNoneSingSvga != null) {
                    mNoneSingSvga.stopAnimation(true);
                }

                if (listener != null) {
                    listener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mNoneSingSvga != null && mNoneSingSvga.isAnimating()) {
                    mNoneSingSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    // 优秀, 目前缺动画
    private void startPerfect(SVGAListener listener) {

    }

    // 不够优秀，换字即可，目前缺动画
    private void startNotPerfect(SVGAListener listener) {

    }
}
