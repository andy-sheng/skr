package com.common.player;

import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.videocache.MediaCacheManager;
import com.google.android.exoplayer2.C;

import java.io.File;
import java.io.IOException;

/**
 * 有的手机播放本地 aac 会没法播放，一 start 立马回调 onCompletion
 */
public class AndroidMediaPlayer extends BasePlayer {
    public static String TAG = "AndroidMediaPlayer";

    // 为了预加载使用
    private static MediaPlayer sPrePlayer;
    private static String mPreLoadUrl;
    private static Handler sUiHanlder = new Handler();

    private MediaPlayer mPlayer;
    private String mPath;

    private boolean mUrlChange = false;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private float mShiftUp = 0;
    private float mVolume = 1.0f;
    private View mView;

    private boolean mHasPrepared = false;

    private long resetTs = 0;
    private long startTs = 0;

    private IPlayerNotSupport mIPlayerNotSupport;

    private boolean mMuted = false;
    private long seekPos = -1;
    private boolean bufferingOk = false;

    public AndroidMediaPlayer() {
        TAG += hashCode();
        MyLog.w(TAG, "AndroidMediaPlayer()");
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
                if (mCallback != null && mHasPrepared) {
                    reset();
                    mCallback.onCompletion();
                }
                mHandler.removeMessages(MSG_DECREASE_VOLUME);
                stopMusicPlayTimeListener();
                long a = System.currentTimeMillis() - startTs;
                if (a > 0 && a < 200) {
                    /**
                     * 一start马上回调onCompletion 可以认为这个播放器不支持这个格式
                     */
                    if (mIPlayerNotSupport != null) {
                        mIPlayerNotSupport.notSupport();
                    }
                }
            }
        });
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                MyLog.d(TAG, "onPrepared begin");
                mHasPrepared = true;
                if (mPlayer != null) {
                    mPlayer.start();
                }
                setVolume(1);
                if (mCallback != null) {
                    mCallback.onPrepared();
                }
                if (mEnableDecreaseVolume) {
                    //mDuration = mp.getDuration();
                    mHandler.removeMessages(MSG_DECREASE_VOLUME);
                    mHandler.sendEmptyMessageDelayed(MSG_DECREASE_VOLUME, 5000);
                }
                if (seekPos > 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            seekTo(seekPos);
                        }
                    });
                }
                startTs = System.currentTimeMillis();
                startMusicPlayTimeListener();
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
        mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if(percent>=100){
                    bufferingOk = true;
                }else{
                    bufferingOk = false;
                }
                mCallback.onBufferingUpdate(mp, percent);
            }
        });
    }

    @Override
    public void setCallback(IPlayerCallback callback) {
        this.mCallback = callback;
        if (callback != null) {
            if (mHasPrepared) {
                callback.onPrepared();
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
    public boolean isBufferingOk() {
        if (mPlayer == null) {
            return false;
        }
        return bufferingOk;
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
        mMuted = isMute;
        if (isMute) {
            mPlayer.setVolume(0, 0);
        } else {
            mPlayer.setVolume(mVolume, mVolume);
        }
    }

    @Override
    public void setVolume(float volume) {
        setVolume(volume, true);
    }

    @Override
    public void setVolume(float volume, boolean setConfig) {
        if (mPlayer == null) {
            return;
        }
        if (setConfig) {
            this.mVolume = volume;
        }
        if (!mMuted) {
            mPlayer.setVolume(volume, volume);
        }
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
    public boolean startPlay(String path) {
        MyLog.d(TAG, "startPlay" + " path=" + path + " oldPath=" + mPath);
        if (mPlayer == null) {
            MyLog.w(TAG, "startPlay but mPlayer === null,return");
            return true;
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
            String p = mPath;
            try {
                MyLog.d(TAG, "startPlay2" + " p=" + p);
//                if (needReset) {
                // 只要重新播 就要 reset 不然状态有问题
                mPlayer.reset();
//                }
                if (p.startsWith("http://") || p.startsWith("https://")) {
//                    p = "http://res-static.inframe.mobi/pkgs/android/guodegang.mp3";
                    mPlayer.setDataSource(MediaCacheManager.INSTANCE.getProxyUrl(p, true));
                } else {
                    mPlayer.setDataSource(p);
                }
                mPlayer.prepareAsync();
            } catch (Exception e) {
                MyLog.e(e);
                MyLog.d(TAG, "startPlay2" + " 有异常，再次尝试");
                try {
                    mPlayer.reset();
                    mPlayer.setDataSource(p);
                    mPlayer.prepareAsync();
                } catch (IOException e1) {
                    MyLog.e(e);
                }
            }
            startMusicPlayTimeListener();
            return true;
        } else {
            mPlayer.start();
            startMusicPlayTimeListener();
            return false;
        }
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
        mHasPrepared = false;
        bufferingOk = false;
        seekPos = -1;
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
        mHasPrepared = false;
        bufferingOk = false;
        seekPos = -1;
        stopMusicPlayTimeListener();
    }

    @Override
    public void release() {
        MyLog.d(TAG, "release");
        if (mPlayer != null) {
            mPlayer.release();
        }
        mHasPrepared = false;
        mPlayer = null;
        sPrePlayer = null;
        mCallback = null;
        mView = null;
        mPath = null;
        seekPos = -1;
        stopMusicPlayTimeListener();
    }

    @Override
    public void seekTo(long msec) {
        MyLog.d(TAG, "seekTo" + " msec=" + msec);
        if (mPlayer == null) {
            return;
        }
        seekPos = msec;
        if (mHasPrepared) {
            mPlayer.seekTo((int) msec);
        }
    }

    @Override
    public void reconnect() {

    }

    @Override
    public float getVolume() {
        return mVolume;
    }

    public void setIPlayerNotSupport(IPlayerNotSupport l) {
        mIPlayerNotSupport = l;
    }


}
