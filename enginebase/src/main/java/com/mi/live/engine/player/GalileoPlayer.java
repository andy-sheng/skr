package com.mi.live.engine.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.base.utils.display.DisplayUtils;
import com.mi.live.engine.base.GalileoDeviceManager;
import com.xiaomi.player.Player;
import com.xiaomi.player.callback.PlayerCallback;
import com.xiaomi.player.datastruct.VideoSize;
import com.xiaomi.player.enums.PlayerSeekingMode;
import com.xiaomi.player.enums.PlayerWorkingMode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static com.xiaomi.player.Player.SurfaceGravity.SurfaceGravityResizeAspectFit;

/**
 * Created by yangli on 2017/9/20.
 *
 * @module 拉流模块，实现引擎拉流功能
 */
public class GalileoPlayer implements IPlayer {
    private static final String TAG = "GalileoPlayer";

    private static final String DEFAULT_PORT = "80";

    private IPlayerCallback mCallback;

    private SurfaceHolder mSurfaceHolder;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;
    private PowerManager.WakeLock mWakeLock = null;

    private long mTransferObserver = 0;
    private PlayerWorkingMode mPlayerMode = PlayerWorkingMode.PlayerWorkingLipSyncMode;

    private String mVideoUrl;
    private String mVideoHost = "";
    private int mVideoWidth;
    private int mVideoHeight;

    private boolean mIsRealTime = false;
    private Player mPlayer;

    private final PlayerCallback mInternalCallback = new PlayerCallback() {
        private boolean mNeedSynthesize = false;
        private boolean mIsBuffering = false;

        @Override
        public void onAudioRenderingStart() {
            MyLog.w(TAG, "onAudioRenderingStart");
        }

        @Override
        public void onVideoRenderingStart() {
            MyLog.w(TAG, "onVideoRenderingStart");
            if (mCallback != null) {
                mCallback.onPrepared();
            }
        }

        @Override
        public void onStartBuffering() {
            MyLog.w(TAG, "onStartBuffering");
            mIsBuffering = true;
            if (mCallback != null) {
                mCallback.onInfo(MEDIA_INFO_BUFFERING_START, 0);
            }
        }

        @Override
        public void onStartPlaying() {
            MyLog.w(TAG, "onStartPlaying");
            mIsBuffering = false;
            if (mCallback != null) {
                mCallback.onInfo(MEDIA_INFO_BUFFERING_END, 0);
            }
        }

        @Override
        public void onPlayerStarted() {
            MyLog.w(TAG, "onPlayerStarted");
        }

        @Override
        public void onPlayerStoped() {
            MyLog.w(TAG, "onPlayerStoped");
        }

        @Override
        public void onPlayerPaused() {
            MyLog.w(TAG, "onPlayerPaused");
            mNeedSynthesize = true;
        }

        @Override
        public void onPlayerResumed() {
            MyLog.w(TAG, "onPlayerResumed");
            if (mCallback != null && mNeedSynthesize) {
                mNeedSynthesize = false;
                mCallback.onInfo(mIsBuffering ? MEDIA_INFO_BUFFERING_START : MEDIA_INFO_BUFFERING_END, 0);
            }
        }

        @Override
        public void onSeekCompleted() {
            MyLog.w(TAG, "onSeekCompleted");
            if (mCallback != null) {
                mCallback.onSeekComplete();
            }
        }

        @Override
        public void onStreamEOF() {
            MyLog.w(TAG, "onStreamEOF");
            if (mCallback != null) {
                mCallback.onCompletion();
            }
        }

        @Override
        public void onOpenStreamFailed() {
            MyLog.w(TAG, "onOpenStreamFailed");
            if (mCallback != null) {
                mCallback.onError(MEDIA_ERROR_CONNECT_SERVER_FAILED, 0);
            }
        }

        @Override
        public void onVideoSizeChanged(VideoSize videoSize) {
            MyLog.w(TAG, "onVideoSizeChanged");
            if (mCallback != null) {
                mCallback.onVideoSizeChanged((int) videoSize.video_width, (int) videoSize.video_height);
            }
        }
    };

    @SuppressLint("Wakelock")
    private void setWakeMode(Context context, int mode) {
        boolean wasHeld = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                wasHeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode | PowerManager.ON_AFTER_RELEASE,
                GalileoPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        if (wasHeld) {
            mWakeLock.acquire();
        }
    }

    @SuppressLint("Wakelock")
    private void stayAwake(boolean awake) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            } else if (!awake && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
        mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
        } else {
            MyLog.w(TAG, "setScreenOnWhilePlaying true is ineffective without a SurfaceHolder");
        }
    }

    public GalileoPlayer(final Context context, final String userId, final String clientIp) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                GalileoDeviceManager.INSTANCE.init(context);
                final String tag = TextUtils.isEmpty(clientIp) ? "" : userId + ":" + clientIp;
                mPlayer = new Player();
                mPlayer.constructPlayer(tag, mInternalCallback, mPlayerMode, mTransferObserver);
                final int screenWidth = GlobalData.screenWidth, screenHeight = GlobalData.screenHeight;
                mPlayer.setGravity(SurfaceGravityResizeAspectFit, screenWidth, screenHeight);
                int curMargin = (GlobalData.screenHeight - GlobalData.screenWidth * 9 / 16) / 2;
                int targetMargin = DisplayUtils.dip2px(140);
                float distance = curMargin - targetMargin;
                mPlayer.shiftUp(distance * 2 / GlobalData.screenHeight, 0.5f, 0.76f, 1.33f, 1.81f);

//                mPlayer.shiftUp((screenHeight - screenWidth * 9 / 16 - DisplayUtils.dip2px(280)) / screenHeight);
                setWakeMode(context, PowerManager.SCREEN_BRIGHT_WAKE_LOCK);
            }
        }, "GalileoPlayer()");
    }

    @Override
    public void setCallback(IPlayerCallback callback) {
        mCallback = callback;
    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public long getDuration() {
        final Player player = mPlayer;
        return player != null ? player.duration() : 0;
    }

    @Override
    public long getCurrentPosition() {
        final Player player = mPlayer;
        return player != null ? player.currentPlaybackTime() : 0;
    }

    @Override
    public long getCurrentStreamPosition() {
        final Player player = mPlayer;
        return player != null ? player.GetCurrentStreamPosition() : 0;
    }

    // TODO 这里读写没在一个线程
    @Override
    public long getCurrentAudioTimestamp() {
        final Player player = mPlayer;
        return player != null ? player.getCurrentAudioTimestamp() : 0l;
    }

    @Override
    public void setSpeedUpThreshold(final long threshold) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setSpeedUpThreshold threshold=" + threshold);
                mPlayer.setSpeedUpThreshold(threshold);
            }
        }, "setSpeedUpThreshold");
    }

    @Override
    public boolean isPlaying() {
        final Player player = mPlayer;
        return player != null && !player.isPaused();
    }

    @Override
    public void setBufferTimeMax(final float timeInSecond) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setBufferTimeMax timeInSecond=" + timeInSecond);
                mPlayer.setBufferTimeMax((long) timeInSecond);
            }
        }, "setBufferTimeMax");
    }

    @Override
    public void setGravity(View view, final Player.SurfaceGravity gravity, final int width, final int height) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setGravity gravity=" + gravity + ", width=" + width + ", height=" + height);
                mPlayer.setGravity(gravity, width, height);
            }
        }, "setGravity");
    }

    @Override
    public void shiftUp(final float ratio, final float min_layer_ratio,
                        final float max_layer_ratio, final float mix_frame_ratio,
                        final float max_frame_ratio) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "shiftUp ratio=" + ratio);
                mPlayer.shiftUp(ratio, min_layer_ratio, max_layer_ratio, mix_frame_ratio, max_frame_ratio);
            }
        }, "shiftUp");
    }

    @Override
    public void setScreenOnWhilePlaying(final boolean screenOn) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mScreenOnWhilePlaying != screenOn) {
                    mScreenOnWhilePlaying = screenOn;
                    updateSurfaceScreenOn();
                }
            }
        }, "setScreenOnWhilePlaying");
    }

    @Override
    public void setMuteAudio(final boolean isMute) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setMuteAudio isMute=" + isMute);
                if (isMute) {
                    mPlayer.muteAudio();
                } else {
                    mPlayer.unMuteAudio();
                }
            }
        }, "setMuteAudio");
    }

    @Override
    public void setVolume(float volumeL, float volumeR) {

    }

    @Override
    public void setSurface(final Surface surface) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setSurface");
                mSurfaceHolder = null;
                mPlayer.setVideoSurface(surface);
                updateSurfaceScreenOn();
            }
        }, "setSurface");
    }

    public void setDisplay(final SurfaceHolder holder) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setDisplay");
                mSurfaceHolder = holder;
                mPlayer.setVideoSurface(holder != null ? holder.getSurface() : null);
                updateSurfaceScreenOn();
            }
        }, "setDisplay");
    }

    @Override
    public void setVideoPath(final String path, final String host) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setVideoPath path=" + path + ", host=" + host);
                mVideoUrl = path;
                mVideoHost = TextUtils.isEmpty(host) ? "" : host;
            }
        }, "setVideoPath");
    }

    @Override
    public void prepare(final boolean realTime) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "prepare realTime=" + realTime);
                mIsRealTime = realTime;
                mPlayer.start(mVideoUrl, mVideoHost, mIsRealTime);
                mPlayer.setSpeaker(true);
            }
        }, "prepare");
    }

    @Override
    public void start() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "start");
                stayAwake(true);
                mPlayer.resume();
            }
        }, "start");
    }

    @Override
    public void pause() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "pause");
                stayAwake(false);
                mPlayer.pause();
            }
        }, "pause");
    }

    @Override
    public void resume() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "start");
                stayAwake(true);
                mPlayer.resume();
            }
        }, "start");
    }

    @Override
    public void stop() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "stop");
                stayAwake(false);
                mPlayer.stop();
            }
        }, "stop");
    }

    @Override
    public void reset() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "reset");
                stayAwake(false);
                mPlayer.stop();
            }
        }, "reset");
    }

    @Override
    public void release() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "release");
                stayAwake(false);
                updateSurfaceScreenOn();
                mVideoWidth = mVideoHeight = 0;
                mPlayer.setVideoSurface(null);
                mPlayer.stop();
                mPlayer.destructPlayer();
                GalileoDeviceManager.INSTANCE.destroy();
            }
        }, "release");
    }

    @Override
    public void seekTo(final long msec) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "seekTo msec=" + msec);
                mPlayer.seekTo(msec, PlayerSeekingMode.PlayerSeekingFastMode);
            }
        }, "seekTo");
    }

    @Override
    public void reconnect() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "reconnect isRealTime=" + mIsRealTime);
                if (mIsRealTime) {
                    mPlayer.reload(mVideoUrl, true);
                } else {
                    mPlayer.seekTo(mPlayer.currentPlaybackTime(), PlayerSeekingMode.PlayerSeekingFastMode);
                }
            }
        }, "reconnect");
    }

    private String getDefaultPortForUrl() {
        return mVideoUrl != null && mVideoUrl.startsWith("http://") ? ":" + DEFAULT_PORT : "";
    }

    private String[] ipListToIpArray(List<String> ipList) {
        if (ipList != null && !ipList.isEmpty()) {
            LinkedHashSet<String> ipSet = new LinkedHashSet<>();
            ipSet.addAll(ipList);
            String[] ipArray = new String[ipSet.size()];
            int i = 0;
            for (String ip : ipSet) {
                ipArray[i++] = ip.contains(":") ? ip : (ip + getDefaultPortForUrl());
            }
            return ipArray;
        } else {
            return new String[0];
        }
    }

    @Override
    public void setIpList(final List<String> httpIpList, final List<String> localIpList) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setIpList");
                List<String> tmpIpList = null;
                if (httpIpList != null && !httpIpList.isEmpty() && localIpList != null && !localIpList.isEmpty()) {
                    tmpIpList = new ArrayList<>();
                    tmpIpList.addAll(localIpList);
                    tmpIpList.removeAll(httpIpList);
                }
                mPlayer.setIpList(ipListToIpArray(httpIpList), ipListToIpArray(tmpIpList));
            }
        }, "setIpList");
    }
}
