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
import com.common.videocache.MediaCacheManager;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.DefaultMediaSourceEventListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
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

import java.io.IOException;

/**
 * Created by chengsimin on 2017/6/1.
 */

public class ExoPlayer extends BasePlayer {
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private static DataSource.Factory mediaDataSourceFactory = new DefaultDataSourceFactory(U.app(), BANDWIDTH_METER,
            new DefaultHttpDataSourceFactory(Util.getUserAgent(U.app(), "SkrExoPlayer"), BANDWIDTH_METER));
    // 为了预加载使用
    private static SimpleExoPlayer sPrePlayer;
    private static String mPreLoadUrl;
    private static MediaSource mPreLoadMediaSource;
    private static Handler sUiHanlder = new Handler();

    private String TAG = "ExoPlayer";

    private SimpleExoPlayer mPlayer;
    private MediaSource mMediaSource;
    private String mUrl;
    private boolean mUrlChange = false;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private float mShiftUp = 0;
    private View mView;
    private float mVolume = 1.0f;
    private boolean mHasPrepared = false;
    private boolean mMuted = false;
    private long pendingSeekPos = -1;
    private boolean bufferingOk = false;

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

        mPlayer.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
                MyLog.d(TAG, "onPlayerStateChanged" + " eventTime=" + eventTime + " playWhenReady=" + playWhenReady + " playbackState=" + playbackState);
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        /**
                         * startPlay 以后会调这个
                         * onPlayerStateChanged playWhenReady=false playbackState=2
                         * onPlayerStateChanged playWhenReady=true playbackState=2
                         */

                        break;
                    case Player.STATE_ENDED:
                        /**
                         * 播放完毕会回调这个
                         */
                        reset();
                        stopMusicPlayTimeListener();
                        if (mCallback != null) {
                            mCallback.onCompletion();
                        }
                        break;
                    case Player.STATE_IDLE:
                        /**
                         * reset会回调这  playWhenReady=false playbackState=1
                         */
                        break;
                    case Player.STATE_READY:
                        /**
                         * 继续播放也会回调这  playWhenReady=true playbackState=3
                         * 暂停 会回调 playWhenReady=false playbackState=3
                         */
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTimelineChanged(EventTime eventTime, int reason) {
                MyLog.d(TAG, "onTimelineChanged" + " eventTime=" + eventTime + " reason=" + reason);
            }

            @Override
            public void onPositionDiscontinuity(EventTime eventTime, int reason) {
                MyLog.d(TAG, "onPositionDiscontinuity" + " eventTime=" + eventTime + " reason=" + reason);
            }

            @Override
            public void onSeekStarted(EventTime eventTime) {
                MyLog.d(TAG, "onSeekStarted" + " eventTime=" + eventTime);
            }

            @Override
            public void onSeekProcessed(EventTime eventTime) {
                MyLog.d(TAG, "onSeekProcessed" + " eventTime=" + eventTime);
                if (mCallback != null) {
                    mCallback.onSeekComplete();
                }
            }

            @Override
            public void onPlaybackParametersChanged(EventTime eventTime, PlaybackParameters playbackParameters) {
                MyLog.d(TAG, "onPlaybackParametersChanged" + " eventTime=" + eventTime + " playbackParameters=" + playbackParameters);
            }

            @Override
            public void onRepeatModeChanged(EventTime eventTime, int repeatMode) {
                MyLog.d(TAG, "onRepeatModeChanged" + " eventTime=" + eventTime + " repeatMode=" + repeatMode);
            }

            @Override
            public void onShuffleModeChanged(EventTime eventTime, boolean shuffleModeEnabled) {
                MyLog.d(TAG, "onShuffleModeChanged" + " eventTime=" + eventTime + " shuffleModeEnabled=" + shuffleModeEnabled);
            }

            @Override
            public void onLoadingChanged(EventTime eventTime, boolean isLoading) {
                MyLog.d(TAG, "onLoadingChanged" + " eventTime=" + eventTime + " isLoading=" + isLoading);
                if (isLoading) {
                    bufferingOk = false;
                    if (mCallback != null) {
                        mCallback.onBufferingUpdate(null, 0);
                    }
                } else {
                    bufferingOk = true;
                    if (mCallback != null) {
                        mCallback.onBufferingUpdate(null, 100);
                    }
                }
            }

            @Override
            public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {
                MyLog.d(TAG, "onPlayerError" + " eventTime=" + eventTime + " error=" + error);
            }

            @Override
            public void onTracksChanged(EventTime eventTime, TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                MyLog.d(TAG, "onTracksChanged" + " eventTime=" + eventTime + " trackGroups=" + trackGroups + " trackSelections=" + trackSelections);
            }

            @Override
            public void onLoadStarted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
                MyLog.d(TAG, "onLoadStarted" + " eventTime=" + eventTime + " loadEventInfo=" + loadEventInfo + " mediaLoadData=" + mediaLoadData);

            }

            @Override
            public void onLoadCompleted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
                MyLog.d(TAG, "onLoadCompleted" + " eventTime=" + eventTime + " loadEventInfo=" + loadEventInfo + " mediaLoadData=" + mediaLoadData);
            }

            @Override
            public void onLoadCanceled(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
                MyLog.d(TAG, "onLoadCanceled" + " eventTime=" + eventTime + " loadEventInfo=" + loadEventInfo + " mediaLoadData=" + mediaLoadData);
            }

            @Override
            public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
                MyLog.d(TAG, "onLoadError" + " eventTime=" + eventTime + " loadEventInfo=" + loadEventInfo + " mediaLoadData=" + mediaLoadData + " error=" + error + " wasCanceled=" + wasCanceled);

            }

            @Override
            public void onDownstreamFormatChanged(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {
                MyLog.d(TAG, "onDownstreamFormatChanged" + " eventTime=" + eventTime + " mediaLoadData=" + mediaLoadData);
            }

            @Override
            public void onUpstreamDiscarded(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {
                MyLog.d(TAG, "onUpstreamDiscarded" + " eventTime=" + eventTime + " mediaLoadData=" + mediaLoadData);

            }

            @Override
            public void onMediaPeriodCreated(EventTime eventTime) {
                MyLog.d(TAG, "onMediaPeriodCreated" + " eventTime=" + eventTime);
            }

            @Override
            public void onMediaPeriodReleased(EventTime eventTime) {
                MyLog.d(TAG, "onMediaPeriodReleased" + " eventTime=" + eventTime);
            }

            @Override
            public void onReadingStarted(EventTime eventTime) {
                MyLog.d(TAG, "onReadingStarted" + " eventTime=" + eventTime);

            }

            @Override
            public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {
                MyLog.d(TAG, "onBandwidthEstimate" + " eventTime=" + eventTime + " totalLoadTimeMs=" + totalLoadTimeMs + " totalBytesLoaded=" + totalBytesLoaded + " bitrateEstimate=" + bitrateEstimate);
            }

            @Override
            public void onSurfaceSizeChanged(EventTime eventTime, int width, int height) {
                MyLog.d(TAG, "onSurfaceSizeChanged" + " eventTime=" + eventTime + " width=" + width + " height=" + height);
            }

            @Override
            public void onMetadata(EventTime eventTime, Metadata metadata) {
                MyLog.d(TAG, "onMetadata" + " eventTime=" + eventTime + " metadata=" + metadata);
            }

            @Override
            public void onDecoderEnabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {
                MyLog.d(TAG, "onDecoderEnabled" + " eventTime=" + eventTime + " trackType=" + trackType + " decoderCounters=" + decoderCounters);
            }

            @Override
            public void onDecoderInitialized(EventTime eventTime, int trackType, String decoderName, long initializationDurationMs) {
                MyLog.d(TAG, "onDecoderInitialized" + " eventTime=" + eventTime + " trackType=" + trackType + " decoderName=" + decoderName + " initializationDurationMs=" + initializationDurationMs);
            }

            @Override
            public void onDecoderInputFormatChanged(EventTime eventTime, int trackType, Format format) {
                MyLog.d(TAG, "onDecoderInputFormatChanged" + " eventTime=" + eventTime + " trackType=" + trackType + " format=" + format);
            }

            @Override
            public void onDecoderDisabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {
                MyLog.d(TAG, "onDecoderDisabled" + " eventTime=" + eventTime + " trackType=" + trackType + " decoderCounters=" + decoderCounters);
            }

            @Override
            public void onAudioSessionId(EventTime eventTime, int audioSessionId) {
                MyLog.d(TAG, "onAudioSessionId" + " eventTime=" + eventTime + " audioSessionId=" + audioSessionId);
                if (mCallback != null) {
                    mCallback.onPrepared();
                }
                mHasPrepared = true;
                setVolume(1);
                if (pendingSeekPos > 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            seekTo(pendingSeekPos);
                        }
                    });
                }
            }

            @Override
            public void onAudioAttributesChanged(EventTime eventTime, AudioAttributes audioAttributes) {
                MyLog.d(TAG, "onAudioAttributesChanged" + " eventTime=" + eventTime + " audioAttributes=" + audioAttributes);
            }

            @Override
            public void onVolumeChanged(EventTime eventTime, float volume) {
                MyLog.d(TAG, "onVolumeChanged" + " eventTime=" + eventTime + " volume=" + volume);
            }

            @Override
            public void onAudioUnderrun(EventTime eventTime, int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
                MyLog.d(TAG, "onAudioUnderrun" + " eventTime=" + eventTime + " bufferSize=" + bufferSize + " bufferSizeMs=" + bufferSizeMs + " elapsedSinceLastFeedMs=" + elapsedSinceLastFeedMs);
            }

            @Override
            public void onDroppedVideoFrames(EventTime eventTime, int droppedFrames, long elapsedMs) {
                MyLog.d(TAG, "onDroppedVideoFrames" + " eventTime=" + eventTime + " droppedFrames=" + droppedFrames + " elapsedMs=" + elapsedMs);
            }

            @Override
            public void onVideoSizeChanged(EventTime eventTime, int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                MyLog.d(TAG, "onVideoSizeChanged" + " width=" + width + " height=" + height + " unappliedRotationDegrees=" + unappliedRotationDegrees + " pixelWidthHeightRatio=" + pixelWidthHeightRatio);
                ExoPlayer.this.videoWidth = width;
                ExoPlayer.this.videoHeight = height;
                if (null != mCallback) {
                    mCallback.onVideoSizeChanged(width, height);
                }
            }

            @Override
            public void onRenderedFirstFrame(EventTime eventTime, @Nullable Surface surface) {
                MyLog.d(TAG, "onRenderedFirstFrame" + " eventTime=" + eventTime + " surface=" + surface);
            }

            @Override
            public void onDrmSessionAcquired(EventTime eventTime) {
                MyLog.d(TAG, "onDrmSessionAcquired" + " eventTime=" + eventTime);
            }

            @Override
            public void onDrmKeysLoaded(EventTime eventTime) {
                MyLog.d(TAG, "onDrmKeysLoaded" + " eventTime=" + eventTime);
            }

            @Override
            public void onDrmSessionManagerError(EventTime eventTime, Exception error) {
                MyLog.d(TAG, "onDrmSessionManagerError" + " eventTime=" + eventTime + " error=" + error);
            }

            @Override
            public void onDrmKeysRestored(EventTime eventTime) {
                MyLog.d(TAG, "onDrmKeysRestored" + " eventTime=" + eventTime);
            }

            @Override
            public void onDrmKeysRemoved(EventTime eventTime) {
                MyLog.d(TAG, "onDrmKeysRemoved" + " eventTime=" + eventTime);
            }

            @Override
            public void onDrmSessionReleased(EventTime eventTime) {
                MyLog.d(TAG, "onDrmSessionReleased" + " eventTime=" + eventTime);
            }
        });
    }

    private static MediaSource buildMediaSource(String path, String overrideExtension) {
        String p;
        if (path.startsWith("http://") || path.startsWith("https://")) {
            p = MediaCacheManager.INSTANCE.getProxyUrl(path, true);
        } else {
            p = path;
        }
        Uri uri = Uri.parse(p);
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
        return mPlayer.getPlayWhenReady();
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
        if (!mMuted) {
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
    public boolean startPlay(String path) {
        MyLog.d(TAG, "startPlay" + " path=" + path);
        if (TextUtils.isEmpty(path)) {
            return true;
        }
        if (mPlayer == null) {
            MyLog.w(TAG, "startPlay but mPlayer === null,return");
            return true;
        }
        if (path != null && !path.equals(mUrl)) {
            mUrl = path;
            mUrlChange = true;
            mMediaSource = buildMediaSource(path, null);
        }
        boolean r = false;
        if (mUrlChange) {
            mUrlChange = false;
            mPlayer.prepare(mMediaSource, true, false);
            r = true;
        } else {
        }
        mPlayer.setPlayWhenReady(true);
        startMusicPlayTimeListener();
        return r;
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
        mHasPrepared = false;
        pendingSeekPos = -1;
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
        pendingSeekPos = -1;
        mHasPrepared = false;
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
        pendingSeekPos = -1;
        stopMusicPlayTimeListener();
    }

    @Override
    public void seekTo(long msec) {
        MyLog.d(TAG, "seekTo" + " msec=" + msec);
        if (mPlayer == null) {
            return;
        }
        if(mHasPrepared){
            mPlayer.seekTo(msec);
        }else{
            pendingSeekPos = msec;
        }
    }

    @Override
    public void reconnect() {

    }

}
