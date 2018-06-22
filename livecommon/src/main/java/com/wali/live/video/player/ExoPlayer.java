package com.wali.live.video.player;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.mi.live.engine.player.IPlayerCallback;
import com.xiaomi.player.Player;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

/**
 * Created by chengsimin on 2017/6/1.
 */

public class ExoPlayer implements IPlayer, IMediaPlayer {
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private static DataSource.Factory mediaDataSourceFactory = new DefaultDataSourceFactory(GlobalData.app(), BANDWIDTH_METER,
            new DefaultHttpDataSourceFactory(Util.getUserAgent(GlobalData.app(), "MiLivePlayer"), BANDWIDTH_METER));

    private String TAG = "ExoPlayer";
    private SimpleExoPlayer mPlayer;
    private MediaSource mMediaSource;
    private String mUrl;
    private boolean mUrlChange = false;
    private int width = 0;
    private int height = 0;

    private OnLoadingChangedListener mOnLoadingChangedListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private OnPreparedListener mOnPreparedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mErrorListener;

    // 为了预加载使用
    private static SimpleExoPlayer sExoPlayer;
    private static String mPreLoadUrl;
    private static MediaSource mPreLoadMediaSource;

    public ExoPlayer() {
        TAG += hashCode();
        MyLog.w(TAG, "ExoPlayer()");
        initializePlayer();
    }

    private static SimpleExoPlayer genPlayer() {
        boolean preferExtensionDecoders = false;

        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;
        // 渲染模块
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(GlobalData.app(),
                null, extensionRendererMode);

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
        return player;
    }

    private void initializePlayer() {
        if (null != sExoPlayer) {
            mPlayer = sExoPlayer;
            mUrl = mPreLoadUrl;
            mMediaSource = mPreLoadMediaSource;
            mPreLoadUrl = "";
            mPreLoadMediaSource = null;
            sExoPlayer = null;
        } else {
            mPlayer = genPlayer();
        }

        mPlayer.addListener(new com.google.android.exoplayer2.ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                MyLog.d(TAG, "onTimelineChanged" + " timeline=" + timeline + " manifest=" + manifest);
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                MyLog.d(TAG, "onTracksChanged" + " trackGroups=" + trackGroups + " trackSelections=" + trackSelections);

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                MyLog.d(TAG, "onLoadingChanged" + " isLoading=" + isLoading);
                if (mOnLoadingChangedListener != null) {
                    mOnLoadingChangedListener.onLoadingChanged(isLoading);
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                MyLog.d(TAG, "onPlayerStateChanged" + " playWhenReady=" + playWhenReady + " playbackState=" + playbackState);

                switch (playbackState) {
                    case com.google.android.exoplayer2.ExoPlayer.STATE_BUFFERING:
                        break;
                    case com.google.android.exoplayer2.ExoPlayer.STATE_ENDED:
                        if (mOnCompletionListener != null) {
                            mOnCompletionListener.onCompletion(ExoPlayer.this);
                        }
                        break;
                    case com.google.android.exoplayer2.ExoPlayer.STATE_IDLE:

                        break;
                    case com.google.android.exoplayer2.ExoPlayer.STATE_READY:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                MyLog.d(TAG, "onPlayerError" + " error=" + error);
                error.printStackTrace();
                if (mErrorListener != null) {
                    mErrorListener.onError(ExoPlayer.this, error.type, 0);
                }
            }

            @Override
            public void onPositionDiscontinuity() {
                MyLog.d(TAG, "onPositionDiscontinuity");

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                MyLog.d(TAG, "onPlaybackParametersChanged" + " playbackParameters=" + playbackParameters);

            }
        });
        mPlayer.setAudioDebugListener(new AudioRendererEventListener() {
            @Override
            public void onAudioEnabled(DecoderCounters counters) {
                MyLog.d(TAG, "onAudioEnabled" + " counters=" + counters);

            }

            @Override
            public void onAudioSessionId(int audioSessionId) {
                MyLog.d(TAG, "onAudioSessionId" + " audioSessionId=" + audioSessionId);

            }

            @Override
            public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
                MyLog.d(TAG, "onAudioDecoderInitialized" + " decoderName=" + decoderName + " initializedTimestampMs=" + initializedTimestampMs + " initializationDurationMs=" + initializationDurationMs);

            }

            @Override
            public void onAudioInputFormatChanged(Format format) {
                MyLog.d(TAG, "onAudioInputFormatChanged" + " format=" + format);

            }

            @Override
            public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
                MyLog.d(TAG, "onAudioTrackUnderrun" + " bufferSize=" + bufferSize + " bufferSizeMs=" + bufferSizeMs + " elapsedSinceLastFeedMs=" + elapsedSinceLastFeedMs);

            }

            @Override
            public void onAudioDisabled(DecoderCounters counters) {
                MyLog.d(TAG, "onAudioDisabled" + " counters=" + counters);

            }
        });
        mPlayer.setVideoDebugListener(new VideoRendererEventListener() {
            @Override
            public void onVideoEnabled(DecoderCounters counters) {
                MyLog.d(TAG, "onVideoEnabled" + " counters=" + counters);
            }

            @Override
            public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
                MyLog.d(TAG, "onVideoDecoderInitialized" + " decoderName=" + decoderName + " initializedTimestampMs=" + initializedTimestampMs + " initializationDurationMs=" + initializationDurationMs);
            }

            @Override
            public void onVideoInputFormatChanged(Format format) {
                MyLog.d(TAG, "onVideoInputFormatChanged" + " format=" + format);

            }

            @Override
            public void onDroppedFrames(int count, long elapsedMs) {
                MyLog.d(TAG, "onDroppedFrames" + " count=" + count + " elapsedMs=" + elapsedMs);

            }

            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                MyLog.d(TAG, "onVideoSizeChanged" + " width=" + width + " height=" + height + " unappliedRotationDegrees=" + unappliedRotationDegrees + " pixelWidthHeightRatio=" + pixelWidthHeightRatio);
                ExoPlayer.this.width = width;
                ExoPlayer.this.height = height;
                if (null != mOnVideoSizeChangedListener) {
                    mOnVideoSizeChangedListener.onVideoSizeChanged(ExoPlayer.this, width, height, 0, 0);
                }
            }

            @Override
            public void onRenderedFirstFrame(Surface surface) {
                MyLog.d(TAG, "onRenderedFirstFrame" + " surface=" + surface);
                EventBus.getDefault().postSticky(new OnPreparedEvent());
                if (mOnPreparedListener != null) {
                    mOnPreparedListener.onPrepared(ExoPlayer.this);
                }
            }

            @Override
            public void onVideoDisabled(DecoderCounters counters) {
                MyLog.d(TAG, "onVideoDisabled" + " counters=" + counters);

            }
        });
        mPlayer.setMetadataOutput(new MetadataRenderer.Output() {
            @Override
            public void onMetadata(Metadata metadata) {
                MyLog.d(TAG, "onMetadata" + " metadata=" + metadata);

            }
        });
    }

    private static MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            // 暂时不支持这些media形式，需要引入新的包
//            case C.TYPE_SS:
//                return new SsMediaSource(uri, ((DemoApplication) getApplication()).buildDataSourceFactory(null),
//                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
//            case C.TYPE_DASH:
//                return new DashMediaSource(uri, ((DemoApplication) getApplication()).buildDataSourceFactory(null),
//                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
//            case C.TYPE_HLS:
//                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        ThreadPool.getUiHandler(), new ExtractorMediaSource.EventListener() {
                    @Override
                    public void onLoadError(IOException error) {
                        MyLog.d("ExoPlayer", "buildMediaSource onLoadError" + " error=" + error);
                    }
                });
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    @Override
    public int getVideoWidth() {
        return width;
    }

    @Override
    public int getVideoHeight() {
        return height;
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
    public long getCurrentStreamPosition() {
        return 0;
    }

    @Override
    public long getCurrentAudioTimestamp() {
        return 0;
    }

    @Override
    public void setSpeedUpThreshold(long threshold) {

    }

    @Override
    public boolean isPlaying() {
        if (mPlayer == null) {
            return false;
        }
        return mPlayer.getPlayWhenReady();
    }

    @Override
    public boolean setRotateDegree(int degree) {
        return false;
    }

    @Override
    public void setBufferTimeMax(float timeInSecond) {

    }

    @Override
    public void reload(String path, boolean flushBuffer) {

    }

    @Override
    public void setGravity(Player.SurfaceGravity gravity, int width, int height) {
        MyLog.d(TAG, "setGravity" + " gravity=" + gravity + " width=" + width + " height=" + height);
        // 这个可以用来设置适应
        if (gravity == Player.SurfaceGravity.SurfaceGravityResizeAspectFill) {
            mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        } else if (gravity == Player.SurfaceGravity.SurfaceGravityResizeAspectFit) {
            mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        } else {
            mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
        }
    }

    @Override
    public void shiftUp(float ratio, float min_layer_ratio, float max_layer_ratio, float mix_frame_ratio, float max_frame_ratio) {

    }

    @Override
    public long getStreamId() {
        return 0;
    }

    @Override
    public long getAudioSource() {
        return 0;
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {

    }

    @Override
    public void prepareAsync(boolean realTime) {
        MyLog.w(TAG, "prepareAsync mMediaSourc=" + mMediaSource + " urlChange:" + mUrlChange);
        if (mUrlChange) {
            mPlayer.prepare(mMediaSource, true, false);
            mPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void setVolume(float volumeL, float volumeR) {
        MyLog.d(TAG, "setVolume" + " volumeL=" + volumeL + " volumeR=" + volumeR);
        if (mPlayer == null) {
            return;
        }
        mPlayer.setVolume(volumeL);
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public void setLooping(boolean looping) {

    }

    @Override
    public void setSurface(Surface surface) {
        MyLog.d(TAG, "setSurface" + " surface=" + surface);
        if (mPlayer == null) {
            return;
        }
        mPlayer.setVideoSurface(surface);
    }

    @Override
    public void setIpList(String[] httpIpList, String[] localIpList) {

    }

    @Override
    public void setDisplay(SurfaceHolder sh) {

    }

    @Override
    public void setTimeout(int prepareTimeout, int readTimeout) {

    }

    @Override
    public void setLogPath(String path) {

    }


    @Override
    public void start() {
        MyLog.d(TAG, "start");
        if (mPlayer == null) {
            return;
        }
        mPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        MyLog.d(TAG, "pause");
        if (mPlayer == null) {
            return;
        }
        mPlayer.setPlayWhenReady(false);
    }

    @Override
    public void resume() {
        MyLog.d(TAG, "resume");
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void stop() {
        MyLog.d(TAG, "stop");
        if (mPlayer == null) {
            return;
        }
        mPlayer.stop();
    }

    @Override
    public void reset() {
        MyLog.d(TAG, "reset");
        if (mPlayer == null) {
            return;
        }
        mPlayer.stop();
    }

    @Override
    public boolean isPlayable() {
        return false;
    }

    @Override
    public void release() {
        MyLog.d(TAG, "release");
        mPlayer.clearVideoSurface();
        mPlayer.release();
        mPlayer.setVideoListener(null);
        mPlayer.setAudioDebugListener(null);
        mPlayer.setVideoDebugListener(null);
        mPlayer = null;
        mMediaSource = null;
        mOnPreparedListener = null;
        mOnCompletionListener = null;
        mOnVideoSizeChangedListener = null;
        sExoPlayer = null;
    }

    @Override
    public String getServerAddress() {
        return null;
    }

    @Override
    public void seekTo(long msec) {
        MyLog.d(TAG, "seekTo" + " msec=" + msec);
        if (mPlayer == null) {
            return;
        }
        mPlayer.seekTo(msec);
    }

    @Override
    public void setIpList(List<String> httpIpList, List<String> localIpList) {

    }

    @Override
    public void setVideoFilter(String filter) {

    }

    @Override
    public void setVideoFilterIntensity(float intensity) {

    }

    @Override
    public void setMp3DataSource(String mp3FilePath, long beginTs, long endTs) {

    }

    @Override
    public void setInnerVolume(float volume) {

    }

    @Override
    public void setMp3Volume(float volume) {

    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        MyLog.w(TAG, "prepareAsync mMediaSourc=" + mMediaSource + " urlChange:" + mUrlChange);
        if (mUrlChange) {
            mPlayer.prepare(mMediaSource, true, false);
            mPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public String getDataSource() {
        MyLog.d(TAG, "getDataSource");
        return mUrl;
    }

    @Override
    public void setDataSource(String path, String host) {
        MyLog.d(TAG, "setDataSource" + " path=" + path + " host=" + host);
        if (path != null && !path.equals(mUrl)) {
            mUrl = path;
            mUrlChange = true;
            mMediaSource = buildMediaSource(Uri.parse(path), null);
        }
    }

    @Override
    public void setOnLoadingChangedListener(OnLoadingChangedListener listener) {
        mOnLoadingChangedListener = listener;
    }

    @Override
    public void setWakeMode(Context context, int mode) {

    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        mErrorListener = listener;
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {

    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {

    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {

    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    public static final class OnPreparedEvent {

    }

    public static void preStartPlayer(String uuid, String clientIp, String url, Object o, Object o1, Object o2, boolean b) {
        MyLog.w("ExoPlayer", "preStartPlayer start " + " uuid=" + uuid + " clientIp=" + clientIp + " url=" + url + " o=" + o + " o1=" + o1 + " o2=" + o2 + " b=" + b);

        PreStartPlayerRunnable preStartPlayerRunnable = new PreStartPlayerRunnable(url);
        preStartPlayerRunnable.run();
    }

    static class PreStartPlayerRunnable implements Runnable {
        String url;

        public PreStartPlayerRunnable(final String url) {
            this.url = url;
        }

        @Override
        public void run() {
            if (sExoPlayer == null) {
                sExoPlayer = genPlayer();
            }
            try {
                mPreLoadUrl = url;
                mPreLoadMediaSource = buildMediaSource(Uri.parse(url), null);
                sExoPlayer.prepare(mPreLoadMediaSource, true, false);
                sExoPlayer.setPlayWhenReady(true);
                MyLog.w("ExoPlayer", "preStartPlayer end");
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }
}
