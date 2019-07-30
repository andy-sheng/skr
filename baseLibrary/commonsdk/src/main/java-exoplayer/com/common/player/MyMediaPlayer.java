package com.common.player;

import android.media.MediaPlayer;
import android.view.Surface;

import com.common.log.MyLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 兼容一些 AndroidMediaPlayer 没法播放的问题
 * 可以使用 INSTANCE 单例播放器，也可以 new 新的播放器
 */
public class MyMediaPlayer implements IPlayer {
    public final String TAG = "MyMediaPlayer";

    AndroidMediaPlayer mAndroidMediaPlayer = new AndroidMediaPlayer();
    ExoPlayer mExoPlayer = new ExoPlayer();

    boolean useAndroidMediaPlayer = true;
    String mPath;
    private IPlayerCallback outCallback = null;

    private IPlayerCallback innerCallback = new IPlayerCallback() {
        @Override
        public void onPrepared() {
            if (outCallback != null) {
                outCallback.onPrepared();
            }
        }

        @Override
        public void onCompletion() {
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

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
        }
    };

    public MyMediaPlayer() {
        mAndroidMediaPlayer.setIPlayerNotSupport(new IPlayerNotSupport() {
            @Override
            public void notSupport() {
                if (useAndroidMediaPlayer) {
                    MyLog.w(TAG, "mAndroidMediaPlayer 没法正常播放，自动切换");
                    mAndroidMediaPlayer.reset();
                    useAndroidMediaPlayer = false;
                    mExoPlayer.startPlay(mPath);
                    mExoPlayer.setVolume(mAndroidMediaPlayer.getVolume());
                }
            }
        });
        mAndroidMediaPlayer.setCallback(innerCallback);
        mExoPlayer.setCallback(innerCallback);
    }

    @Override
    public void setCallback(IPlayerCallback callback) {
        outCallback = callback;
    }


    @Override
    public int getVideoWidth() {
        if (useAndroidMediaPlayer) {
            return mAndroidMediaPlayer.getVideoWidth();
        } else {
            return mExoPlayer.getVideoWidth();
        }
    }

    @Override
    public int getVideoHeight() {
        if (useAndroidMediaPlayer) {
            return mAndroidMediaPlayer.getVideoHeight();
        } else {
            return mExoPlayer.getVideoHeight();
        }
    }

    @Override
    public long getDuration() {
        if (useAndroidMediaPlayer) {
            return mAndroidMediaPlayer.getDuration();
        } else {
            return mExoPlayer.getDuration();
        }
    }

    @Override
    public long getCurrentPosition() {
        if (useAndroidMediaPlayer) {
            return mAndroidMediaPlayer.getCurrentPosition();
        } else {
            return mExoPlayer.getCurrentPosition();
        }
    }

    @Override
    public boolean isPlaying() {
        if (useAndroidMediaPlayer) {
            return mAndroidMediaPlayer.isPlaying();
        } else {
            return mExoPlayer.isPlaying();
        }
    }

    @Override
    public void setGravity(Object view, int gravity, int width, int height) {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.setGravity(view, gravity, width, height);
        } else {
            mExoPlayer.setGravity(view, gravity, width, height);
        }
    }

    @Override
    public void shiftUp(float ratio, float min_layer_ratio, float max_layer_ratio, float mix_frame_ratio, float max_frame_ratio) {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.shiftUp(ratio, min_layer_ratio, max_layer_ratio, mix_frame_ratio, max_frame_ratio);
        } else {
            mExoPlayer.shiftUp(ratio, min_layer_ratio, max_layer_ratio, mix_frame_ratio, max_frame_ratio);
        }
    }

    @Override
    public void setMuteAudio(boolean isMute) {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.setMuteAudio(isMute);
        } else {
            mExoPlayer.setMuteAudio(isMute);
        }
    }

    @Override
    public void setVolume(float volume) {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.setVolume(volume);
        } else {
            mExoPlayer.setVolume(volume);
        }
    }

    @Override
    public void setVolume(float volume, boolean set) {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.setVolume(volume, set);
        } else {
            mExoPlayer.setVolume(volume, set);
        }
    }

    @Override
    public void setSurface(Surface surface) {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.setSurface(surface);
        } else {
            mExoPlayer.setSurface(surface);
        }
    }

    @Override
    public boolean startPlay(String path) {
        mPath = path;
        if (useAndroidMediaPlayer) {
            return mAndroidMediaPlayer.startPlay(path);
        } else {
            return mExoPlayer.startPlay(path);
        }
    }

    @Override
    public void startPlayPcm(String path, int channels, int sampleRate, int byteRate) {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.startPlayPcm(path, channels, sampleRate, byteRate);
        } else {
            mExoPlayer.startPlayPcm(path, channels, sampleRate, byteRate);
        }
    }

    @Override
    public void pause() {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.pause();
        } else {
            mExoPlayer.pause();
        }
    }

    @Override
    public void resume() {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.resume();
        } else {
            mExoPlayer.resume();
        }
    }

    @Override
    public void stop() {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.stop();
        } else {
            mExoPlayer.stop();
        }
    }

    @Override
    public void reset() {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.reset();
        } else {
            mExoPlayer.reset();
        }
    }

    @Override
    public void release() {
        mAndroidMediaPlayer.release();
        mExoPlayer.release();
    }

    @Override
    public void seekTo(long msec) {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.seekTo(msec);
        } else {
            mExoPlayer.seekTo(msec);
        }
    }

    @Override
    public void reconnect() {
        if (useAndroidMediaPlayer) {
            mAndroidMediaPlayer.reconnect();
        } else {
            mExoPlayer.reconnect();
        }
    }

    @Override
    public float getVolume() {
        if (useAndroidMediaPlayer) {
            return mAndroidMediaPlayer.getVolume();
        } else {
            return mExoPlayer.getVolume();
        }
    }

    @Override
    public void setDecreaseVolumeEnd(boolean b) {
        mAndroidMediaPlayer.setDecreaseVolumeEnd(b);
        mExoPlayer.setDecreaseVolumeEnd(b);
    }

    @Override
    public void setMonitorProgress(boolean b) {
        mAndroidMediaPlayer.setMonitorProgress(b);
        mExoPlayer.setMonitorProgress(b);
    }
}
