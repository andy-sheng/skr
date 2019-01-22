package com.component.busilib.manager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import com.common.log.MyLog;
import com.common.player.IPlayerCallback;
import com.common.player.exoplayer.ExoPlayer;
import com.common.utils.U;

public class BgMusicManager {

    public final static String TAG = "BgMusicManager";

    public static final String PREF_KEY_PIPEI_VOLUME_SWITCH = "pref_pipei_volume_switch";   // 音量开关
    public static final String PREF_KEY_PIPEI_VOLUME = "pref_pipei_volume";                 // 音量百分比

    boolean mIsPlay;
    float mMaxVolume;  // 最大音量

    ExoPlayer mExoPlayer;
    ValueAnimator mAnimator;  // 用来做淡入的效果

    private BgMusicManager() {
        mIsPlay = U.getPreferenceUtils().getSettingBoolean(PREF_KEY_PIPEI_VOLUME_SWITCH, true);
        mMaxVolume = U.getPreferenceUtils().getSettingInt(PREF_KEY_PIPEI_VOLUME, 100) / 100f;
    }

    private static class BgMusicManagerrHolder {
        private static final BgMusicManager INSTANCE = new BgMusicManager();
    }

    public static final BgMusicManager getInstance() {
        return BgMusicManagerrHolder.INSTANCE;
    }

    public boolean isPlay() {
        return mIsPlay;
    }

    public void setPlay(boolean play) {
        mIsPlay = play;
    }

    public float getMaxVolume() {
        return mMaxVolume;
    }

    public void setMaxVolume(float maxVolume) {
        this.mMaxVolume = maxVolume;
    }

    public void starPlay(final String path, final long msec) {
        if (!mIsPlay) {
            MyLog.d(TAG, "starPlay" + " isPlay = false ");
            return;
        }
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
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofFloat(0f, mMaxVolume);
            mAnimator.setDuration(3000);
            mAnimator.setInterpolator(new LinearInterpolator());
        }
        mAnimator.removeAllUpdateListeners();
        mAnimator.removeAllListeners();
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (valueAnimator.getAnimatedValue() instanceof Float) {
                    if (mExoPlayer != null) {
                        mExoPlayer.setVolume((float) valueAnimator.getAnimatedValue());
                    }
                }
            }
        });

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mExoPlayer != null) {
                    mExoPlayer.setVolume(mMaxVolume);
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
        mAnimator.start();
    }


    public void destory() {
        if (mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer = null;
        }

        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }
}
