package com.component.busilib.manager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import com.common.log.MyLog;
import com.common.player.IPlayerCallback;
import com.common.player.exoplayer.ExoPlayer;

public class BgMusicManager {

    public final static String TAG = "BgMusicManager";

    ExoPlayer mExoPlayer;
    ValueAnimator animator;  // 用来做淡入的效果

    private BgMusicManager() {

    }

    private static class BgMusicManagerrHolder {
        private static final BgMusicManager INSTANCE = new BgMusicManager();
    }

    public static final BgMusicManager getInstance() {
        return BgMusicManagerrHolder.INSTANCE;
    }

    public void starPlay(final String path, final long msec) {
        if (mExoPlayer != null && mExoPlayer.isPlaying()) {
            mExoPlayer.stop();
        }
        if (mExoPlayer == null) {
            mExoPlayer = new ExoPlayer();
        }
        mExoPlayer.startPlay(path);
        mExoPlayer.seekTo(msec);

        mExoPlayer.setCallback(new IPlayerCallback() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void onCompletion() {
                starPlay(path, msec);
            }

            @Override
            public void onSeekComplete() {

            }

            @Override
            public void onVideoSizeChanged(int width, int height) {

            }

            @Override
            public void onError(int what, int extra) {

            }

            @Override
            public void onInfo(int what, int extra) {

            }
        });

        initVolume();
    }

    public boolean isPlaying() {
        if (mExoPlayer != null && mExoPlayer.isPlaying()) {
            return true;
        }

        return false;
    }

    private void initVolume() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(3000);
            animator.setInterpolator(new LinearInterpolator());
        }
        animator.removeAllUpdateListeners();
        animator.removeAllListeners();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (valueAnimator.getAnimatedValue() instanceof Float) {
                    if (mExoPlayer != null) {
                        mExoPlayer.setVolume((float) valueAnimator.getAnimatedValue());
                    }
                }
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mExoPlayer != null) {
                    mExoPlayer.setVolume(1f);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }


    public void destory() {
        if (mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer = null;
        }

        if (animator != null) {
            animator.cancel();
        }
    }
}
