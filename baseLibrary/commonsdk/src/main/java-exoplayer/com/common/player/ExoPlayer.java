package com.common.player;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.U;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.source.DefaultMediaSourceEventListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
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

import java.io.IOException;

/**
 * Created by chengsimin on 2017/6/1.
 */

public class ExoPlayer extends BasePlayer {
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private static DataSource.Factory mediaDataSourceFactory = new DefaultDataSourceFactory(U.app(), BANDWIDTH_METER,
            new DefaultHttpDataSourceFactory(Util.getUserAgent(U.app(), "MiLivePlayer"), BANDWIDTH_METER));
    // 为了预加载使用
    private static SimpleExoPlayer sPrePlayer;
    private static String mPreLoadUrl;
    private static MediaSource mPreLoadMediaSource;
    private static Handler sUiHanlder = new Handler();

    private String TAG = "ExoPlayer";

    private IPlayerCallback mCallback;

    private SimpleExoPlayer mPlayer;
    private MediaSource mMediaSource;
    private String mUrl;
    private boolean mUrlChange = false;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private float mShiftUp = 0;
    private View mView;
    private float mVolume = 1.0f;
    private boolean mPreparedFlag = false;
    private boolean mMuted = false;

    public ExoPlayer() {
        TAG += hashCode();
        MyLog.w(TAG, "ExoPlayer()");
        initializePlayer();
    }

    private static SimpleExoPlayer genPlayer() {

        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;
        // 渲染模块
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(U.app(),
                null, extensionRendererMode);

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
        return player;
    }

    private void initializePlayer() {
        if (null != sPrePlayer) {
            mPlayer = sPrePlayer;
            mUrl = mPreLoadUrl;
            mMediaSource = mPreLoadMediaSource;
            mPreLoadUrl = "";
            mPreLoadMediaSource = null;
            sPrePlayer = null;
        } else {
            mPlayer = genPlayer();
        }

        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
                MyLog.d(TAG, "onTimelineChanged" + " timeline=" + timeline + " manifest=" + manifest + " reason=" + reason);
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                MyLog.d(TAG, "onTracksChanged" + " trackGroups=" + trackGroups + " trackSelections=" + trackSelections);
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                MyLog.d(TAG, "onLoadingChanged" + " isLoading=" + isLoading);

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                MyLog.d(TAG, "onPlayerStateChanged" + " playWhenReady=" + playWhenReady + " playbackState=" + playbackState);
                switch (playbackState) {
                    case com.google.android.exoplayer2.ExoPlayer.STATE_BUFFERING:
                        break;
                    case com.google.android.exoplayer2.ExoPlayer.STATE_ENDED:
                        if (mCallback != null) {
                            mCallback.onCompletion();
                        }
                        reset();
                        stopMusicPlayTimeListener();
                        mHandler.removeMessages(MSG_DECREASE_VOLUME);
                        MyLog.d(TAG, "onCompletion");
                        break;
                    case com.google.android.exoplayer2.ExoPlayer.STATE_IDLE:

                        break;
                    case com.google.android.exoplayer2.ExoPlayer.STATE_READY:
                        if (mCallback != null) {
                            mCallback.onPrepared();
                        } else {
                            mPreparedFlag = true;
                        }
                        setVolume(1);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                MyLog.d(TAG, "onRepeatModeChanged" + " repeatMode=" + repeatMode);

            }


            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                MyLog.d(TAG, "onShuffleModeEnabledChanged" + " shuffleModeEnabled=" + shuffleModeEnabled);


            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                MyLog.d(TAG, "onPlayerError" + " error=" + error);
                error.printStackTrace();
                if (mCallback != null) {
                    mCallback.onError(-1, -1);
                }
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                MyLog.d(TAG, "onPositionDiscontinuity" + " reason=" + reason);

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                MyLog.d(TAG, "onPlaybackParametersChanged" + " playbackParameters=" + playbackParameters);

            }

            @Override
            public void onSeekProcessed() {
                MyLog.d(TAG, "onSeekProcessed");

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
            public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
                MyLog.d(TAG, "onAudioSinkUnderrun" + " bufferSize=" + bufferSize + " bufferSizeMs=" + bufferSizeMs + " elapsedSinceLastFeedMs=" + elapsedSinceLastFeedMs);
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
                ExoPlayer.this.videoWidth = width;
                ExoPlayer.this.videoHeight = height;
                if (null != mCallback) {
                    mCallback.onVideoSizeChanged(width, height);
                }
            }

            @Override
            public void onRenderedFirstFrame(Surface surface) {
                MyLog.d(TAG, "onRenderedFirstFrame" + " surface=" + surface);
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
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, sUiHanlder, new DefaultMediaSourceEventListener() {
                    @Override
                    public void onMediaPeriodCreated(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
                        super.onMediaPeriodCreated(windowIndex, mediaPeriodId);
                        MyLog.d("ExoPlayer", "onMediaPeriodCreated" + " windowIndex=" + windowIndex + " mediaPeriodId=" + mediaPeriodId);
                    }

                    @Override
                    public void onMediaPeriodReleased(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
                        super.onMediaPeriodReleased(windowIndex, mediaPeriodId);
                        MyLog.d("ExoPlayer", "onMediaPeriodReleased" + " windowIndex=" + windowIndex + " mediaPeriodId=" + mediaPeriodId);

                    }

                    @Override
                    public void onLoadStarted(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
                        super.onLoadStarted(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData);
                        MyLog.d("ExoPlayer", "onLoadStarted" + " windowIndex=" + windowIndex + " mediaPeriodId=" + mediaPeriodId + " loadEventInfo=" + loadEventInfo + " mediaLoadData=" + mediaLoadData);
                    }

                    @Override
                    public void onLoadCompleted(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
                        super.onLoadCompleted(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData);
                        MyLog.d("ExoPlayer", "onLoadCompleted" + " windowIndex=" + windowIndex + " mediaPeriodId=" + mediaPeriodId + " loadEventInfo=" + loadEventInfo + " mediaLoadData=" + mediaLoadData);
                    }

                    @Override
                    public void onLoadCanceled(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
                        super.onLoadCanceled(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData);
                        MyLog.d("ExoPlayer", "onLoadCanceled" + " windowIndex=" + windowIndex + " mediaPeriodId=" + mediaPeriodId + " loadEventInfo=" + loadEventInfo + " mediaLoadData=" + mediaLoadData);
                    }

                    @Override
                    public void onLoadError(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
                        super.onLoadError(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData, error, wasCanceled);
                        MyLog.d("ExoPlayer", "onLoadError" + " windowIndex=" + windowIndex + " mediaPeriodId=" + mediaPeriodId + " loadEventInfo=" + loadEventInfo + " mediaLoadData=" + mediaLoadData + " error=" + error + " wasCanceled=" + wasCanceled);
                    }

                    @Override
                    public void onReadingStarted(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
                        super.onReadingStarted(windowIndex, mediaPeriodId);
                        MyLog.d("ExoPlayer", "onReadingStarted" + " windowIndex=" + windowIndex + " mediaPeriodId=" + mediaPeriodId);
                    }

                    @Override
                    public void onUpstreamDiscarded(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
                        super.onUpstreamDiscarded(windowIndex, mediaPeriodId, mediaLoadData);
                        MyLog.d("ExoPlayer", "onUpstreamDiscarded" + " windowIndex=" + windowIndex + " mediaPeriodId=" + mediaPeriodId + " mediaLoadData=" + mediaLoadData);
                    }

                    @Override
                    public void onDownstreamFormatChanged(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
                        super.onDownstreamFormatChanged(windowIndex, mediaPeriodId, mediaLoadData);
                        MyLog.d("ExoPlayer", "onDownstreamFormatChanged" + " windowIndex=" + windowIndex + " mediaPeriodId=" + mediaPeriodId + " mediaLoadData=" + mediaLoadData);
                    }
                });
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        sUiHanlder, new ExtractorMediaSource.EventListener() {
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
    public void setCallback(IPlayerCallback callback) {
        this.mCallback = callback;
        if (callback != null) {
            if (mPreparedFlag) {
                callback.onPrepared();
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
        return mPlayer.getPlayWhenReady();
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
            mPlayer.setVolume(0);
        } else {
            mPlayer.setVolume(mVolume);
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
        if(!mMuted){
            mPlayer.setVolume(volume);
        }
    }

    public float getVolume() {
        return mVolume;
    }

    @Override
    public void setDecreaseVolumeEnd(boolean b) {
        mEnableDecreaseVolume = b;
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
    public void startPlay(String path) {
        MyLog.d(TAG, "startPlay" + " path=" + path);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        if (mPlayer == null) {
            MyLog.w(TAG, "startPlay but mPlayer === null,return");
            return;
        }
        if (path != null && !path.equals(mUrl)) {
            mUrl = path;
            mUrlChange = true;
            mMediaSource = buildMediaSource(Uri.parse(path), null);
        }
        if (mUrlChange) {
            mUrlChange = false;
            mPlayer.prepare(mMediaSource, true, false);
        }
        mPlayer.setPlayWhenReady(true);
        startMusicPlayTimeListener();
    }

    @Override
    public void startPlayPcm(String path, int channels, int sampleRate, int byteRate) {
        throw new IllegalArgumentException("Exoplayer not support PCM");
    }

    @Override
    public void pause() {
        MyLog.d(TAG, "pause");
        if (mPlayer == null) {
            return;
        }
        mPlayer.setPlayWhenReady(false);
        stopMusicPlayTimeListener();
    }

    @Override
    public void resume() {
        MyLog.d(TAG, "resume");
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(true);
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
        mUrl = null;
        stopMusicPlayTimeListener();
    }

    @Override
    public void reset() {
        MyLog.d(TAG, "reset");
        if (mPlayer == null) {
            return;
        }
        mPlayer.stop();
        mUrl = null;
        stopMusicPlayTimeListener();
    }

    @Override
    public void release() {
        MyLog.d(TAG, "release");
        if (mPlayer != null) {
            mPlayer.clearVideoSurface();
            mPlayer.release();
            mPlayer.setVideoListener(null);
            mPlayer.setAudioDebugListener(null);
            mPlayer.setVideoDebugListener(null);
        }
        mPlayer = null;
        mMediaSource = null;
        sPrePlayer = null;
        mCallback = null;
        mView = null;
        mUrl = null;
        stopMusicPlayTimeListener();
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
    public void reconnect() {

    }

}
