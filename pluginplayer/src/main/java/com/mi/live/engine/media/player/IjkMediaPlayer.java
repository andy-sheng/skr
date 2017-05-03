/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mi.live.engine.media.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.base.log.MyLog;
import com.xiaomi.player.Player;
import com.xiaomi.player.callback.PlayerCallback;
import com.xiaomi.player.datastruct.VideoSize;
import com.xiaomi.player.enums.PlayerWorkingMode;

import java.io.IOException;


/**
 * @author bbcallen
 *         <p>
 *         Java wrapper of ffplay.
 */
public final class IjkMediaPlayer extends AbstractMediaPlayer {
    private final static String TAG = "IjkMediaPlayer";

    private SurfaceHolder mSurfaceHolder;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoSarNum;
    private int mVideoSarDen;

    private String mDataSource;
    private Player m_player;
    private PlayerCallback m_pc;
    private Context m_appContext;
    private String m_videoUrl;
    private String m_videoHost = "";
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private void registerCallback() {
        m_pc = new PlayerCallback() {
            @Override
            public void onAudioRenderingStart() {
                MyLog.w(TAG, "debug::onAudioRenderingStart ");
            }

            @Override
            public void onVideoRenderingStart() {
                MyLog.w(TAG, "debug::onVideoRenderingStart");
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyOnPrepared();
                        }
                    });
                }
            }

            @Override
            public void onStartBuffering() {
                MyLog.w(TAG, "debug::onStartBuffering ");
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyOnInfo(MEDIA_INFO_BUFFERING_START, 0);
                        }
                    });
                }
            }

            @Override
            public void onStartPlaying() {
                MyLog.w(TAG, "debug::onStartPlaying ");
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyOnInfo(MEDIA_INFO_BUFFERING_END, 0);
                        }
                    });
                }
            }

            @Override
            public void onPlayerStarted() {
                MyLog.w(TAG, "debug::onPlayerStarted ");
            }

            @Override
            public void onPlayerStoped() {
                MyLog.w(TAG, "debug::onPlayerStoped ");
            }

            @Override
            public void onPlayerPaused() {
                MyLog.w(TAG, "debug::onPlayerPaused ");
            }

            @Override
            public void onPlayerResumed() {
                MyLog.w(TAG, "debug::onPlayerResumed ");
            }

            @Override
            public void onSeekCompleted() {
                MyLog.w(TAG, "debug::onSeekCompleted ");
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyOnSeekComplete();
                        }
                    });
                }
            }

            @Override
            public void onStreamEOF() {
                MyLog.w(TAG, "debug::onStreamEOF ");
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyOnCompletion();
                        }
                    });
                }
            }

            @Override
            public void onOpenStreamFailed() {
                MyLog.w(TAG, "debug::onOpenStreamFailed ");
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyOnError(MEDIA_ERROR_CONNECT_SERVER_FAILED, 0);
                        }
                    });
                }
            }

            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                MyLog.w(TAG, "debug::onVideoSizeChanged ");
                final VideoSize vs = videoSize;
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mVideoHeight = (int) vs.video_height;
                            mVideoWidth = (int) vs.video_width;
                            notifyOnVideoSizeChanged((int) vs.video_width, (int) vs.video_height, 0, 0);
                        }
                    });
                }
            }
        };
    }

    public IjkMediaPlayer(Context context, String tag, PlayerWorkingMode mode, long IStreamTransferObserver) {
        m_appContext = context;
        registerCallback();
        m_player = new Player();
        MyLog.w(TAG, "debug::initPlayer with app context=" + m_appContext + ", mode=" + mode + ", IStreamTransferObserver=" + IStreamTransferObserver);
        m_player.constructPlayer(tag, m_pc, mode, IStreamTransferObserver);
        setWakeMode(context, PowerManager.SCREEN_BRIGHT_WAKE_LOCK);
    }

    /**
     * Default constructor. Consider using one of the create() methods for
     * synchronously instantiating a IjkMediaPlayer from a Uri or resource.
     * <p>
     * When done with the IjkMediaPlayer, you should call {@link #release()}, to
     * free the resources. If not released, too many IjkMediaPlayer instances
     * may result in an exception.
     * </p>
     */
    public IjkMediaPlayer(Context context, String videoUrl) {
        m_appContext = context;
        m_videoUrl = videoUrl;
        MyLog.w(TAG, "debug::IjkMediaPlayer with context:" + context + " videoUrl:" + videoUrl);
    }

    /*
     * Update the IjkMediaPlayer SurfaceTexture. Call after setting a new
     * display surface.
     */
    // private native void _setVideoSurface(Surface surface);

    /**
     * Sets the {@link SurfaceHolder} to use for displaying the video portion of
     * the media.
     * <p>
     * Either a surface holder or surface must be set if a display or video sink
     * is needed. Not calling this method or {@link #setSurface(Surface)} when
     * playing back a video will result in only the audio track being played. A
     * null surface holder or surface will result in only the audio track being
     * played.
     *
     * @param sh the SurfaceHolder to use for video display
     */
    @Override
    public void setDisplay(SurfaceHolder sh) {
        MyLog.w(TAG, "setDisplay");
        mSurfaceHolder = sh;
        Surface surface;
        if (sh != null) {
            surface = sh.getSurface();
        } else {
            surface = null;
        }
        //_setVideoSurface(surface);
        m_player.setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    @Override
    public void setDataSource(String s, String host) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        m_videoUrl = s;
        m_videoHost = TextUtils.isEmpty(host) ? "" : host;
        MyLog.w(TAG, "setDataSource url=" + m_videoUrl + ", host=" + m_videoHost);
    }

    @Override
    public String getDataSource() {
        return m_videoUrl;
    }

    /**
     * Sets the {@link Surface} to be used as the sink for the video portion of
     * the media. This is similar to {@link #setDisplay(SurfaceHolder)}, but
     * does not support {@link #setScreenOnWhilePlaying(boolean)}. Setting a
     * Surface will un-set any Surface or SurfaceHolder that was previously set.
     * A null surface will result in only the audio track being played.
     * <p>
     * If the Surface sends frames to a {@link SurfaceTexture}, the timestamps
     * returned from {@link SurfaceTexture#getTimestamp()} will have an
     * unspecified zero point. These timestamps cannot be directly compared
     * between different media sources, different instances of the same media
     * source, or multiple runs of the same program. The timestamp is normally
     * monotonically increasing and is unaffected by time-of-day adjustments,
     * but it is reset when the position is set.
     *
     * @param surface The {@link Surface} to be used for the video portion of the
     *                media.
     */
    @Override
    public void setSurface(Surface surface) {
        MyLog.w(TAG, "setSurface");
        if (mScreenOnWhilePlaying && surface != null) {
            MyLog.w(TAG,
                    "setScreenOnWhilePlaying(true) is ineffective for Surface");
        }
        mSurfaceHolder = null;
        m_player.setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    @Override
    public void setIpList(String[] httpIpList, String[] localIpList) {
        MyLog.w(TAG, "setIpList");
        m_player.setIpList(httpIpList, localIpList);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        MyLog.w(TAG, "prepareAsync");
        stayAwake(true);
        m_player.start(m_videoUrl, m_videoHost, false);
        m_player.setSpeaker(true);
    }


    @Override
    public void start() throws IllegalStateException {
        MyLog.w(TAG, "start");
        stayAwake(true);
        m_player.resume();
    }

    public void prepareAsync(boolean realTime) throws IllegalStateException {
        MyLog.w(TAG, "prepareAsync: realTime=" + realTime);
        stayAwake(true);
        m_player.start(m_videoUrl, m_videoHost, realTime);
        m_player.setSpeaker(true);
    }

    @Override
    public void stop() throws IllegalStateException {
        MyLog.w(TAG, "stop");
        stayAwake(false);
        m_player.stop();
    }


    @Override
    public void pause() throws IllegalStateException {
        MyLog.w(TAG, "pause");
        stayAwake(false);
        m_player.pause();
    }


    @SuppressLint("Wakelock")
    @Override
    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                washeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode | PowerManager.ON_AFTER_RELEASE,
                IjkMediaPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        if (washeld) {
            mWakeLock.acquire();
        }
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mScreenOnWhilePlaying != screenOn) {
            if (screenOn && mSurfaceHolder == null) {
                MyLog.w(TAG,
                        "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
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
        }
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
    public boolean isPlaying() {
        return !m_player.isPaused();
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        MyLog.w(TAG, "seekTo: msec=" + msec);
        m_player.seekTo(msec);
    }

    @Override
    public long getCurrentPosition() {
        return m_player.currentPlaybackTime();
    }

    @Override
    public long getDuration() {
        return m_player.duration();
    }


    @Override
    public void release() {
        MyLog.w(TAG, "release");
        stayAwake(false);
        updateSurfaceScreenOn();
        resetListeners();
        mVideoWidth = 0;
        mVideoHeight = 0;
        m_player.setVideoSurface(null);
        m_player.stop();
        m_player.destructPlayer();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }


    @Override
    public void reset() {
        MyLog.w(TAG, "reset");
        stayAwake(false);
        m_player.stop();
    }

    @Override
    public void setVolume(float v, float v1) {
        MyLog.w(TAG, "setVolume: left=" + v + ", right=" + v1);
        if (v > 0 || v1 > 0) {
            m_player.unMuteAudio();
        } else {
            m_player.muteAudio();
        }
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    public long getCurrentStreamPosition() {
        return m_player.GetCurrentStreamPosition();
    }

    public boolean reload(String url, boolean realTime) {
        MyLog.w(TAG, "reload: url=" + url + ", realTime=" + realTime);
        return m_player.reload(url, realTime);
    }

    public void setBufferTimeMax(long timeSecond) {
        MyLog.w(TAG, "setBufferTimeMax: timeSecond=" + timeSecond);
        m_player.setBufferTimeMax(timeSecond);
    }

    public void setGravity(Player.SurfaceGravity gravity, int width, int height) {
        MyLog.w(TAG, "setGravity: gravity=" + gravity + ", width=" + width + ", height=" + height);
        m_player.setGravity(gravity, width, height);
    }

    public void shiftUp(float ratio) {
        MyLog.w(TAG, "shiftUp: ratio=" + ratio);
        m_player.shiftUp(ratio);
    }

    public long getStreamId() {
        MyLog.w(TAG, "getStreamId");
        return m_player.getStreamId();
    }

    public long getAudioSource() {
        MyLog.w(TAG, "getAudioSource");
        return m_player.getAudioTransfer();
    }

    public void addRecordingSession(long recordingSession) {
        m_player.addRecordingSession(recordingSession);
    }

    public void removeRecordingSession(long recordingSession) {
        m_player.removeRecordingSession(recordingSession);
    }

    /**
     * 获取当前播放视频帧的时间戳
     *
     * @return
     */
    public long getTimestampOfCurrentVideoFrame() {
        return m_player.getTimestampOfCurrentVideoFrame();
    }
}
