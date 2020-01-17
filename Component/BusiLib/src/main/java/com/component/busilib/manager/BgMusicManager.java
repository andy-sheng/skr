package com.component.busilib.manager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.text.TextUtils;
import android.view.animation.LinearInterpolator;

import com.common.log.MyLog;
import com.common.player.ExoPlayer;
import com.common.player.PlayerCallbackAdapter;
import com.common.player.SinglePlayer;
import com.common.player.SinglePlayerCallbackAdapter;
import com.common.utils.U;

public class BgMusicManager {

    public final String TAG = "BgMusicManager";

    public static final String PREF_KEY_PIPEI_VOLUME_SWITCH = "pref_pipei_volume_switch";   // 音量开关
    public static final String PREF_KEY_PIPEI_VOLUME = "pref_pipei_volume";                 // 音量百分比

    float mMaxVolume;  // 最大音量
    boolean mIsRoom = false;   // 是否在房间内，可以直接在GraMatchSucessFragment

    ValueAnimator mAnimator;  // 用来做淡入的效果

    private BgMusicManager() {
        mMaxVolume = U.getPreferenceUtils().getSettingInt(PREF_KEY_PIPEI_VOLUME, 100) / 100f;
    }

    private static class BgMusicManagerHolder {
        private static final BgMusicManager INSTANCE = new BgMusicManager();
    }

    public static final BgMusicManager getInstance() {
        return BgMusicManagerHolder.INSTANCE;
    }

    public void setMaxVolume(float maxVolume) {
        this.mMaxVolume = maxVolume;
    }

    public void starPlay(final String path, final long msec, final String from) {
        starPlay(path, msec, from, 0);
    }

    public void starPlay(final String path, final long msec, final String from, int deep) {
        MyLog.d(TAG, "starPlay" + " path=" + path + " msec=" + msec + " from=" + from);
        if (deep >= 10) {
            return;
        }
        // 音效开关
        if (!U.getPreferenceUtils().getSettingBoolean(PREF_KEY_PIPEI_VOLUME_SWITCH, true)) {
            MyLog.d(TAG, "starPlay" + " isPlay = false ");
            return;
        }

        if (mIsRoom) {
            MyLog.d(TAG, "starPlay" + " mIsRoom = true ");
            return;
        }

        if (TextUtils.isEmpty(path)) {
            MyLog.d(TAG, "starPlay" + " path = null ");
            return;
        }

        SinglePlayer.INSTANCE.startPlay(TAG, path);
        if (msec != 0) {
            SinglePlayer.INSTANCE.seekTo(TAG, msec);
        }

        SinglePlayer.INSTANCE.addCallback(TAG, new SinglePlayerCallbackAdapter() {
            @Override
            public void onCompletion() {
                starPlay(path, msec, from, deep + 1);
            }
        });

        initVolume();
    }

    public boolean isRoom() {
        return mIsRoom;
    }

    public void setRoom(boolean room) {
        if (room != mIsRoom) {
            mIsRoom = room;
            if (mIsRoom) {
                // 如果切换到房间内了，destroy保护一下
                destory();
            }
        }
    }

    public boolean isPlaying() {
        if (SinglePlayer.INSTANCE.getStartFrom() == TAG && SinglePlayer.INSTANCE.isPlaying()) {
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
                    SinglePlayer.INSTANCE.setVolume((float) valueAnimator.getAnimatedValue());
                }
            }
        });

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                SinglePlayer.INSTANCE.setVolume(mMaxVolume);
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
        MyLog.d(TAG, "destory");
        SinglePlayer.INSTANCE.stop(TAG);
        SinglePlayer.INSTANCE.removeCallback(TAG);
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }
}
