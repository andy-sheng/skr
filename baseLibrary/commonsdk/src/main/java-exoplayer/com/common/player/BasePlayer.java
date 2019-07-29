package com.common.player;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.common.log.MyLog;
import com.common.player.event.PlayerEvent;
import com.common.utils.HandlerTaskTimer;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class BasePlayer implements IPlayer {

    public final String TAG = "BasePlayer";

    protected boolean mEnableDecreaseVolume = false;
    protected boolean mMonitorProgress = false;

    protected HandlerTaskTimer mMusicTimePlayTimeListener;

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_DECREASE_VOLUME) {
                int t = (int) (getDuration() - getCurrentPosition() - 3000);
                if (t > 0) {
                    /**
                     * 如果在prepare 直接获取 duration 会有问题，直接error 回调 onComplete ，所以延迟5秒获取 duration
                     */
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            decreaseVolume();
                        }
                    }, t);
                }
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
        mEnableDecreaseVolume = b;
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

    @Override
    public void setMonitorProgress(boolean b) {
        mMonitorProgress = b;
    }

    protected void startMusicPlayTimeListener() {
        if (!mMonitorProgress) {
            return;
        }
        if (mMusicTimePlayTimeListener != null) {
            mMusicTimePlayTimeListener.dispose();
        }
        mMusicTimePlayTimeListener = HandlerTaskTimer.newBuilder()
                .delay(1000)
                .interval(1000)
                .start(new Observer<Integer>() {
                    long duration = -1;

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        if(isPlaying()){
                            long currentPostion = getCurrentPosition();
                            if (duration < 0) {
                                duration = getDuration();
                            }
                            PlayerEvent.TimeFly engineEvent = new PlayerEvent.TimeFly();
                            engineEvent.totalDuration = duration;
                            engineEvent.curPostion = currentPostion;
                            if(engineEvent.curPostion>0 && engineEvent.totalDuration>0){
                                EventBus.getDefault().post(engineEvent);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    protected void stopMusicPlayTimeListener() {
        if (mMusicTimePlayTimeListener != null) {
            mMusicTimePlayTimeListener.dispose();
        }
    }
}
