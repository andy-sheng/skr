package com.common.player.mediaplayer;

import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.player.IPlayer;
import com.common.player.IPlayerCallback;
import com.common.player.event.PlayerEvent;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.google.android.exoplayer2.C;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class AndroidMediaPlayer implements IPlayer {
    public static String TAG = "AndroidMediaPlayer";

    // 为了预加载使用
    private static MediaPlayer sPrePlayer;
    private static String mPreLoadUrl;
    private static Handler sUiHanlder = new Handler();

    private IPlayerCallback mCallback;
    private MediaPlayer mPlayer;
    private String mPath;

    private boolean mUrlChange = false;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private float mShiftUp = 0;
    private long mDuration;
    private boolean enableDecreaseVolume = false;

    private View mView;

    private boolean mPreparedFlag = false;

    HandlerTaskTimer mMusicTimePlayTimeListener;

    long resetTs = 0;

    Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG_DECREASE_VOLUME){
                decreaseVolume();
            }
        }
    };

    public static final int MSG_DECREASE_VOLUME = 10;

    public AndroidMediaPlayer() {
        TAG += hashCode();
        MyLog.w(TAG, "ExoPlayer()");
        initializePlayer();
    }

    private static MediaPlayer genPlayer() {
        return new MediaPlayer();
    }

    private void initializePlayer() {
        if (null != sPrePlayer) {
            mPlayer = sPrePlayer;
            mPath = mPreLoadUrl;
            mPreLoadUrl = "";
            sPrePlayer = null;
        } else {
            mPlayer = genPlayer();
        }
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MyLog.d(TAG, "onCompletion" + " mp=" + mp);
                /**
                 * 只要调用了reset 接口也会异步回调这个 ，这不是期望的
                 *  所以使用时间戳保护一下
                 */
                if (mCallback != null && !TextUtils.isEmpty(mPath) && (System.currentTimeMillis() - resetTs)>500){
                    mCallback.onCompletion();
                }
            }
        });
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mDuration = mp.getDuration();
                MyLog.d(TAG, "onPrepared 总时长:"+ mDuration);
                setVolume(1);
                if (mPlayer != null) {
                    mPlayer.start();
                }
                if (mCallback != null) {
                    mCallback.onPrepared(mDuration);
                }
                if(enableDecreaseVolume){
                    mHandler.removeMessages(MSG_DECREASE_VOLUME);
                    mHandler.sendEmptyMessageDelayed(MSG_DECREASE_VOLUME, mDuration -3000);
                }
            }
        });
        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                MyLog.d(TAG, "onError" + " what=" + what + " extra=" + extra);
                if (mCallback != null) {
                    mCallback.onError(what, extra);
                }
                return false;
            }
        });
        mPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                MyLog.d(TAG, "onVideoSizeChanged" + " width=" + width + " height=" + height);
                AndroidMediaPlayer.this.videoWidth = width;
                AndroidMediaPlayer.this.videoHeight = height;
                if (null != mCallback) {
                    mCallback.onVideoSizeChanged(width, height);
                }
            }
        });
        mPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                MyLog.d(TAG, "onSeekComplete");
                if (null != mCallback) {
                    mCallback.onSeekComplete();
                }
            }
        });
    }

    @Override
    public void setCallback(IPlayerCallback callback) {
        this.mCallback = callback;
        if (callback != null) {
            if (mPreparedFlag) {
                callback.onPrepared(mDuration);
                mPreparedFlag = false;
            }
        }
    }

    @Override
    public int getVideoWidth() {
        return videoWidth;
    }

    @Override
    public int getVideoHeight() {
        return videoHeight;
    }

    @Override
    public long getDuration() {
        if (mPlayer == null) {
            return 0;
        }
        return mPlayer.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        if (mPlayer == null) {
            return 0;
        }
        return mPlayer.getCurrentPosition();
    }

    @Override
    public boolean isPlaying() {
        if (mPlayer == null) {
            return false;
        }
        return mPlayer.isPlaying();
    }

    @Override
    public void setGravity(Object view, int gravity, int width, int height) {
        MyLog.d(TAG, "setGravity" + " gravity=" + gravity + " width=" + width + " height=" + height);
        if (view instanceof View) {
            mView = (View) view;
        }

        // 这个可以用来设置适应
        if (gravity == GRAVITY_FIT_WITH_CROPPING) {
            mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        } else {
            mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        }
        adjustView("setGravity");
    }

    /**
     * 调整 video view 视图
     *
     * @param from
     */
    private void adjustView(String from) {
        MyLog.d(TAG, "adjustView" + " from=" + from);

        // ExoPlayer不会跟GalioPlayer那样自动留黑适配等，需要自己把TextView设定成适配流的大小
        if (mView != null) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mView.getLayoutParams();
            MyLog.d(TAG, "adjustView videoWidth:" + videoWidth + " videoHeight:" + videoHeight);
            MyLog.d(TAG, "adjustView lp.width:" + lp.width + " lp.height:" + lp.height);
            MyLog.d(TAG, "adjustView view.width:" + mView.getWidth() + " view.height:" + mView.getHeight());
            View parent = (View) mView.getParent();
            MyLog.d(TAG,
                    "adjustView parent.width:" + parent.getWidth() + " parent.height:" + parent.getHeight());
            if (videoHeight != 0 && videoWidth != 0 && parent.getWidth() != 0) {
                //目的：将view弄成合适的宽度和高度
                //判断流是横屏还是竖屏

                //假设以宽度为基准适配
                int height = parent.getWidth() * videoHeight / videoWidth;
                MyLog.d(TAG, "adjustView 计算出height=" + height);
                if (height > parent.getHeight()) {
                    MyLog.d(TAG, "adjustView" + " 高为准，两边留黑");
                    // 超出了父布局的高度了，说明以宽度适配不合适，改为高度适配
                    int width = parent.getHeight() * videoWidth / videoHeight;
                    MyLog.d(TAG, "adjustView width=" + width);
                    lp.width = width;
                    lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp.leftMargin = (parent.getWidth() - width) / 2;
                    lp.topMargin = 0;
                    mView.setLayoutParams(lp);
                } else if (height <= parent.getHeight()) {
                    MyLog.d(TAG, "adjustView" + " 宽为准，上下留黑");
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp.height = height;
                    lp.leftMargin = 0;

                    // 这样乘以一个数的原因是，不清楚GalioPlayer 的 ratio 是怎么划算的，同样的0.17两边表现不一样，不做一些衰减，shiftUp太大了。
                    int shiftUp = (int) (mShiftUp * parent.getHeight() * 0.5f);
                    lp.topMargin = (parent.getHeight() - height) / 2 - shiftUp;
                    mView.setLayoutParams(lp);
                }
            }
        }
    }

    @Override
    public void shiftUp(float ratio, float min_layer_ratio, float max_layer_ratio, float mix_frame_ratio, float max_frame_ratio) {
        MyLog.d(TAG, "shiftUp" + " ratio=" + ratio + " min_layer_ratio=" + min_layer_ratio + " max_layer_ratio=" + max_layer_ratio + " mix_frame_ratio=" + mix_frame_ratio + " max_frame_ratio=" + max_frame_ratio);
        mShiftUp = ratio;
        adjustView("shiftUp");
    }


    @Override
    public void setMuteAudio(boolean isMute) {
        if (isMute) {
            mPlayer.setVolume(0, 0);
        } else {
            mPlayer.setVolume(0.5f, 0.5f);
        }
    }

    float mVolume = 1.0f;

    @Override
    public void setVolume(float volume) {
        if (mPlayer == null) {
            return;
        }
        mVolume = volume;
        mPlayer.setVolume(volume, volume);
    }

    @Override
    public void setVolume(float volume, boolean setConfig) {
        if (mPlayer == null) {
            return;
        }
        if (setConfig) {
            this.mVolume = volume;
        }
        mPlayer.setVolume(volume, volume);
    }

    @Override
    public void setSurface(Surface surface) {
        MyLog.d(TAG, "setSurface" + " surface=" + surface);
        if (mPlayer == null) {
            return;
        }
        mPlayer.setSurface(surface);
    }

    /**
     * 也可以播放在线的文件
     *
     * @param path
     */
    @Override
    public void startPlay(String path) {
        MyLog.d(TAG, "startPlay" + " path=" + path);
        if (mPlayer == null) {
            MyLog.w(TAG, "startPlay but mPlayer === null,return");
            return;
        }
        boolean needReset = false;
        if (path != null && !path.equals(mPath)) {
            if (!TextUtils.isEmpty(mPath)) {
                needReset = true;
            }
            mPath = path;
            mUrlChange = true;
        }
        if (mUrlChange) {
            mUrlChange = false;
            try {
                String p = mPath;
                MyLog.d(TAG, "startPlay2" + " p=" + p);
                if (needReset) {
                    reset();
                }
                mPlayer.setDataSource(p);
                mPlayer.prepareAsync();
            } catch (IOException e) {
            }
        }

        // 同步播放
//        mPlayer.prepareAsync();
//        mPlayer.start();
        startMusicPlayTimeListener();
    }

    @Override
    public void startPlayPcm(String path, int channels, int sampleRate, int byteRate) {
        String p = mPath;
        File file = new File(path);
        String ext = U.getFileUtils().getSuffixFromFilePath(file.getPath());
        if (ext.equals("pcm")) {
            // 是PCM 转一下。
            String name = U.getFileUtils().getFileNameFromFilePathWithoutExt(file.getPath());
            File destFile = new File(file.getParentFile(), name + ".wav");
            p = destFile.getAbsolutePath();
            try {
                U.getMediaUtils().rawToWave(file, destFile, channels, sampleRate, byteRate);
            } catch (IOException e) {
                MyLog.e(e);
            }
        }
        startPlay(p);
    }

    @Override
    public void pause() {
        MyLog.d(TAG, "pause");
        if (mPlayer == null) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        stopMusicPlayTimeListener();
    }

    @Override
    public void resume() {
        MyLog.d(TAG, "resume");
        if (mPlayer != null) {
            mPlayer.start();
        }
        startMusicPlayTimeListener();
    }

    @Override
    public void stop() {
        MyLog.d(TAG, "stop");
        if (mPlayer == null) {
            return;
        }
        mPlayer.stop();
        mPath = null;
        mHandler.removeCallbacksAndMessages(null);
        stopMusicPlayTimeListener();
    }

    @Override
    public void reset() {
        MyLog.d(TAG, "reset");
        if (mPlayer == null) {
            return;
        }
        resetTs = System.currentTimeMillis();
        mPlayer.reset();
        mPath = null;
        mHandler.removeCallbacksAndMessages(null);
        stopMusicPlayTimeListener();
    }

    @Override
    public void release() {
        MyLog.d(TAG, "release");
        mPlayer.release();
        mPlayer = null;
        sPrePlayer = null;
        mCallback = null;
        mView = null;
        mPath = null;
        mHandler.removeCallbacksAndMessages(null);
        stopMusicPlayTimeListener();
    }

    @Override
    public void seekTo(long msec) {
        MyLog.d(TAG, "seekTo" + " msec=" + msec);
        if (mPlayer == null) {
            return;
        }
        mPlayer.seekTo((int) msec);
    }

    @Override
    public void reconnect() {

    }

    @Override
    public float getVolume() {
        return mVolume;
    }

    @Override
    public void setDecreaseVolumeEnd(boolean b) {
        enableDecreaseVolume = b;
    }

    private void startMusicPlayTimeListener() {
        if (mMusicTimePlayTimeListener != null) {
            mMusicTimePlayTimeListener.dispose();
        }
        mMusicTimePlayTimeListener = HandlerTaskTimer.newBuilder().interval(1000)
                .start(new Observer<Integer>() {
                    long duration = -1;

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        long currentPostion = getCurrentPosition();
                        if (duration < 0) {
                            duration = getDuration();
                        }
                        PlayerEvent.TimeFly engineEvent = new PlayerEvent.TimeFly();
                        engineEvent.totalDuration = duration;
                        engineEvent.curPostion = currentPostion;
                        EventBus.getDefault().post(engineEvent);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void stopMusicPlayTimeListener() {
        if (mMusicTimePlayTimeListener != null) {
            mMusicTimePlayTimeListener.dispose();
        }
    }

    ValueAnimator mDecreaseVolumeAnimator;

    private void decreaseVolume(){
        MyLog.d(TAG,"decreaseVolume" );
        if (mDecreaseVolumeAnimator != null) {
            mDecreaseVolumeAnimator.cancel();
        }
        mDecreaseVolumeAnimator = ValueAnimator.ofFloat(getVolume(),0);
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
}
