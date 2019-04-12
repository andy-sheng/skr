package com.module.playways.grab.room.top;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.common.utils.HandlerTaskTimer;
import com.module.rank.R;

/**
 * 房主说话时底部 点点点 动画view
 */
public class SpeakingTipsAnimationView extends AppCompatImageView {
    public final static String TAG = "SpeakingDotAnimationView";

    int mAnimationRes[] = new int[]{
            R.drawable.yuyin_shengwen1,
            R.drawable.yuyin_shengwen2,
            R.drawable.yuyin_shengwen3,
    };

    public SpeakingTipsAnimationView(Context context) {
        super(context);
        init();
    }

    public SpeakingTipsAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {

    }

    HandlerTaskTimer mAnimationTask;

    public void show(int duration) {
        int interval = 50;
        setVisibility(VISIBLE);
        cancel(false);
        mAnimationTask = HandlerTaskTimer.newBuilder().interval(200)
                .take(duration / interval)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        setImageResource(mAnimationRes[integer % mAnimationRes.length]);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        cancel(true);
                    }
                });
    }

    public void cancel(boolean hide) {
        if (mAnimationTask != null) {
            mAnimationTask.dispose();
        }
        if (hide) {
            setVisibility(GONE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancel(false);
    }
}
