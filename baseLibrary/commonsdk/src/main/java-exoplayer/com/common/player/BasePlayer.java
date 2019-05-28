package com.common.player;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.common.log.MyLog;

public abstract class BasePlayer implements IPlayer {

    public final static String TAG = "BasePlayer";

    protected boolean enableDecreaseVolume = false;

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_DECREASE_VOLUME) {
                decreaseVolume();
            }
        }
    };

    protected ValueAnimator mDecreaseVolumeAnimator;
    public static final int MSG_DECREASE_VOLUME = 10;

    protected void decreaseVolume() {
        MyLog.d("BasePlayer", "decreaseVolume");
        if (mDecreaseVolumeAnimator != null) {
            mDecreaseVolumeAnimator.cancel();
        }
        mDecreaseVolumeAnimator = ValueAnimator.ofFloat(getVolume(), 0);
        mDecreaseVolumeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                setVolume(v, false);
            }
        });
        mDecreaseVolumeAnimator.setDuration(3000);
        mDecreaseVolumeAnimator.start();
    }

    @Override
    public void setDecreaseVolumeEnd(boolean b) {
        enableDecreaseVolume = b;
    }


    @Override
    public void stop() {
        if (mDecreaseVolumeAnimator != null) {
            mDecreaseVolumeAnimator.cancel();
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void reset() {
        if (mDecreaseVolumeAnimator != null) {
            mDecreaseVolumeAnimator.cancel();
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void release() {
        if (mDecreaseVolumeAnimator != null) {
            mDecreaseVolumeAnimator.cancel();
        }
        mHandler.removeCallbacksAndMessages(null);
    }
}
