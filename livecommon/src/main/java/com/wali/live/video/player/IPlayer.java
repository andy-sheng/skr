package com.wali.live.video.player;

import android.view.Surface;
import android.view.SurfaceHolder;

import com.xiaomi.player.Player;

import java.io.IOException;
import java.util.List;

/**
 * Created by chenyong on 16/7/11.
 */
public interface IPlayer {

    boolean setRotateDegree(int degree);

    void setBufferTimeMax(float timeSecond);

    void reload(String path, boolean flushBuffer);

    void setSurface(Surface surface);

    void setDisplay(SurfaceHolder sh);

    void setTimeout(int prepareTimeout, int readTimeout);

    void setLogPath(String path);

    void setOnPreparedListener(IMediaPlayer.OnPreparedListener listener);

    void setOnCompletionListener(IMediaPlayer.OnCompletionListener listener);

    void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener listener);

    void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener listener);

    void setOnVideoSizeChangedListener(IMediaPlayer.OnVideoSizeChangedListener listener);

    void setOnErrorListener(IMediaPlayer.OnErrorListener listener);

    void setOnInfoListener(IMediaPlayer.OnInfoListener listener);

    void setDataSource(String path, String host) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setScreenOnWhilePlaying(boolean screenOn);

    void prepareAsync(boolean realTime);

    int getVideoWidth();

    int getVideoHeight();

    void seekTo(long msec);

    void start();

    void reset();

    boolean isPlaying();

    void pause();

    long getDuration();

    void stop();

    void release();

    String getServerAddress();

    long getCurrentPosition();

    void setVolume(float volumeL, float volumeR);

    void setBufferSize(int size);

    void setLooping(boolean looping);

    long getCurrentStreamPosition();

    long getCurrentAudioTimestamp();

    void setSpeedUpThreshold(long threshold);

    void setGravity(Player.SurfaceGravity gravity, int width, int height);

    void shiftUp(float ratio, float min_layer_ratio, float max_layer_ratio, float mix_frame_ratio, float max_frame_ratio);

    long getStreamId();

    long getAudioSource();

    void setIpList(List<String> httpIpList, List<String> localIpList);

    void setVideoFilter(String filter);

    void setVideoFilterIntensity(float intensity);

    void setMp3DataSource(String mp3FilePath, long beginTs, long endTs);

    void setInnerVolume(float volume);

    void setMp3Volume(float volume);

    void resume();
}
