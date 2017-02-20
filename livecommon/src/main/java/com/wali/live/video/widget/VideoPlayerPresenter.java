package com.wali.live.video.widget;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Surface;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.Constants;
import com.base.utils.sdcard.SDCardUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.engine.media.player.IMediaPlayer;
import com.mi.live.engine.media.player.MediaInfo;
import com.mi.live.engine.media.player.util.PlayConfig;
import com.mi.live.engine.player.GalileoPlayer;
import com.mi.live.engine.player.IPlayer;
import com.wali.live.dns.PreDnsManager;
import com.wali.live.video.presenter.FixedStreamerDebugPresenter;
import com.xiaomi.player.Player;
import com.xiaomi.player.enums.PlayerWorkingMode;

import java.io.File;
import java.io.IOException;
import java.util.List;

import rx.Subscription;

/**
 * Created by linjinbin on 16/7/6.
 *
 * @module 播放器模块，所有播放器统一使用的控制器
 */
public class VideoPlayerPresenter implements IPlayerPresenter {
    public static final String TAG = VideoPlayerPresenter.class.getSimpleName();

    //TODO 自定义
    public static final int INTERRUPT_MODE_DEFAULT = -1;
    public static final String NEED_BLACK_IDENTIFY_STR = "playui=";

    //TODO 这些状态仿照金山代码,目前来看没有具体作用
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // 中断模式,暂时使用自己添加的
    private int mInterruptMode = INTERRUPT_MODE_DEFAULT;
    private int mCurrentState = STATE_IDLE;

    // id标志
    private String mLiveId;
    private String mDumpPath;

    private Uri mUri;
    private String mHost;
    private long mDuration;
    private MediaInfo mMediaInfo;

    private IPlayer mPlayer = null;
    public static final Object MEDIA_PLAYER_LOCK = new Object();
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private Player.SurfaceGravity mSurfaceGravity;

    // 外部注册的监听回调
    private IPlayerCallBack mPlayerCallBack;
    private boolean mIsWatch = false;
    private boolean mIsStreamerDebug = FixedStreamerDebugPresenter.getsInstance().isStreamerDebug();

    private int mPlayMode = VideoPlayMode.PLAY_MODE_LANDSCAPE;

    //TODO notice : surface重新调用
    private boolean mIsNeedReset = true;
    // 是否重绘背景
    private boolean mIsClearCanvas = true;

    // 保留上次停止的状态
    private boolean mLastStopped = false;
    // 保留上次暂停的网络状态
    private boolean mWifiNetwork = false;

    // 调试信息
    private long mStartTime = 0;
    private long mPauseStartTime = 0;
    private long mPausedTime = 0;
    private long mFirstAudioTime = 0;

    private String mBitrateStr;
    private String mCurBitRateStr;
    private String mCachedAudioDurationStr;
    private String mDownloadSizeStr;
    private String mCacheVideoBytes;
    private String mCacheAudioBytes;
    private String mCacheVideoDuration;
    private String mCacheAudioDuration;

    private String mIpStr;
    private String mResolutionStr;
    private String mMediaMetaStr;
    private String mFrameStr;
    private String mPrepareStr = "";
    private String mStreamName;

    private String mIpAddress;
    AudioManager mAudioManager;

    private long mGetDecodedDataTime;
    private long mDecodedDataSize;

    private float mVolumeL = 1, mVolumeR = 1;
    private int mBufferSize = 0;
    private boolean mLooping = false;
    private IVideoView mVideoView;

    private SurfaceTexture mSurfaceTexture = null;
    private Surface mSurface;
    private Paint mClearPaint;

    private boolean mIsLandscape = false;

    private boolean mRealTime = false;
    private long mTransferObserver = 0;
    private PlayerWorkingMode mPlayerMode = PlayerWorkingMode.PlayerWorkingLipSyncMode;

    PowerManager.WakeLock mWakeLock;

    private boolean mIsReconnectEnable = true;

    public VideoPlayerPresenter(int videoWidth, int videoHeight, boolean realTime) {
        init(videoWidth, videoHeight, realTime);
    }

    private void init(int videoWidth, int videoHeight, boolean realTime) {
        mVideoHeight = videoHeight;
        mVideoWidth = videoWidth;
        mRealTime = realTime;
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public void setIsWatch(boolean isWatch) {
        mIsWatch = isWatch;
    }

    @Override
    public void enableReconnect(boolean isEnable) {
        mIsReconnectEnable = isEnable;
    }

    @Override
    public boolean isEnableReconnect() {
        return mIsReconnectEnable;
    }

    public void setView(IVideoView videoView) {
        mVideoView = videoView;
    }

    // 初始化设置，无后续操作
    @Override
    public void setPlayMode(int playMode) {
        mPlayMode = playMode;
    }

    @Override
    public int getPlayMode() {
        return mPlayMode;
    }

    public boolean setRotateDegree(int degree) {
        return mPlayer != null && mPlayer.setRotateDegree(degree);
    }

    public void setNeedReset(boolean isNeedReset) {
        mIsNeedReset = isNeedReset;
    }

    public void setClearCanvas(boolean clearCanvas) {
        mIsClearCanvas = clearCanvas;
    }

    public void setVideoStreamBufferTime(float bufferTime) {
        if (mPlayer != null) {
            mPlayer.setBufferTimeMax(bufferTime);
        }
    }

    @Override
    public void setVideoPath(String path, String host) {
        path = getVideoPathForDebug(path);
        MyLog.w(TAG, "setVideoPath path=" + path + ", host=" + host);
        if (!TextUtils.isEmpty(path)) {
            mUri = Uri.parse(path);
            mHost = host;
            setStreamName(path);
        }
    }

    @Override
    public void setVideoPath(String liveId, String path, String host) {
        path = getVideoPathForDebug(path);
        MyLog.w(TAG, "setVideoPath path=" + path + ", host=" + host);
        mLiveId = liveId;
        mDumpPath = SDCardUtils.getKsyPath() + "/" +
                mLiveId + "-" + System.currentTimeMillis() + ".flv";
        if (!TextUtils.isEmpty(path)) {
            setStreamName(path);
            setVideoURI(Uri.parse(path), host);
        } else {
            MyLog.e(TAG, "setVideoPath but path is empty");
        }
    }

    @Override
    public void setVideoPath(String liveId, String path, String host, int interruptMode) {
        path = getVideoPathForDebug(path);
        MyLog.w(TAG, "setVideoPath path=" + path + ", host=" + host + ", interruptMode=" + interruptMode);
        setStreamName(path);
        mLiveId = liveId;
        mInterruptMode = interruptMode;
        mDumpPath = SDCardUtils.getKsyPath() + "/" +
                mLiveId + "-" + System.currentTimeMillis() + ".flv";
        setVideoURI(Uri.parse(path), host);
    }

    private String getVideoPathForDebug(String path) {
        if (mIsWatch && mIsStreamerDebug) {
            if (!TextUtils.isEmpty(FixedStreamerDebugPresenter.getsInstance().getFixedWatchUrl())) {
                path = FixedStreamerDebugPresenter.getsInstance().getFixedWatchUrl();//"http://163.177.43.12/r2.zb.mi.com/live/stream_400.flv";
            } else if (!TextUtils.isEmpty(FixedStreamerDebugPresenter.getsInstance().getFixedWatchIp())) {
                path = PreDnsManager.replaceIp(path, FixedStreamerDebugPresenter.getsInstance().getFixedWatchIp());
            }
        }
        return path;
    }

    private void setVideoURI(Uri uri, String host) {
        mUri = uri;
        mHost = host;
        openVideo();
        mVideoView.onSetVideoURICompleted();
        MyLog.w(TAG, "setVideoURI over");
    }

    private void setStreamName(String path) {
        if (!path.isEmpty() && path.contains(".flv")) {
            mStreamName = path.substring(0, path.lastIndexOf(".flv"));
        }
    }

    public void rotateVideo(int rotateAngle) {
        if (mPlayer != null) {
            mPlayer.setRotateDegree(rotateAngle);
        }
    }

    public String getStreamName() {
        return mStreamName;
    }

    /**
     * 直接使用金山提供的reload接口进行重连
     */
    @Override
    public void reconnect() {
        if (null != mPlayer && null != mUri && mIsReconnectEnable) {
            MyLog.w(TAG, "reload uri=" + mUri.toString());
            try {
                mPlayer.reload(mUri.toString(), mRealTime);
            } catch (OutOfMemoryError error) {
                MyLog.e(TAG, error);
            }
        } else {
            MyLog.w(TAG, "reconnect condition is wrong");
        }
    }

    private void setSurface() {
        if (mPlayer != null) {
            if (mSurfaceTexture != null) {
                if (mSurface == null) {
                    mSurface = new Surface(mSurfaceTexture);
                }
                mPlayer.setSurface(mSurface);
                MyLog.w(TAG, "setSurfaceTexture");
            } else if (null != mVideoView.getSurfaceHolder()) {
                mPlayer.setDisplay(mVideoView.getSurfaceHolder());
                MyLog.w(TAG, "setDisplay");
            }
        }
    }

    public void openVideo() {
        MyLog.w(TAG, "openVideo");
        if (mUri == null) {
            MyLog.d(TAG, "url or mSurfaceHolder is null return");
            return;
        }

        // 停止其他音乐服务
        stopOtherMusic();

        try {
            mDuration = -1;
            mMediaInfo = null;

            if (mPlayer == null) {
                mPlayer = new GalileoPlayer(GlobalData.app(), mPlayerMode, mTransferObserver, UserAccountManager.getInstance().getUuid(), MiLinkClientAdapter.getsInstance().getClientIp());
                mPlayer.setBufferTimeMax(Constants.PLAYER_BUFFER_TIME);//设置缓冲时间5s
                mPlayer.setTimeout(5, 5);
                mPlayer.setVolume(mVolumeL, mVolumeR);
                if (Constants.isDebugBuild) {
                    //每次设置路径,先清理下文件夹
                    SDCardUtils.clearKsyDumpFile();
//                    mPlayer.setOption(KSYMediaPlayer.OPT_CATEGORY_FORMAT,
//                            "dump_file_name", mDumpPath);

                }
                String logPath = CommonUtils.getUniqueFilePath(new File(SDCardUtils.getKsyLogPath()), System.currentTimeMillis() + ".log");
                if (!TextUtils.isEmpty(logPath)) {
                    mPlayer.setLogPath(logPath);
                }

                mPlayer.setOnPreparedListener(mPreparedListener);
                mPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
                mPlayer.setOnCompletionListener(mCompletionListener);
                mPlayer.setOnErrorListener(mErrorListener);
                mPlayer.setOnInfoListener(mInfoListener);
                mPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            }
            mPlayer.setDataSource(mUri.toString(), mHost);

            if (mVideoView != null) {
                setSurface();
                mVideoView.adjustVideoLayout(mIsLandscape);
            }
            if (mViewWidth > 0 && mViewHeight > 0 && mSurfaceGravity != null) {
                mPlayer.setGravity(mSurfaceGravity, mViewWidth, mViewHeight);
            } else {
                updateGravity();
            }
            mPlayer.setScreenOnWhilePlaying(true);
            mPlayer.prepareAsync(mRealTime);
            if (mPlayerCallBack != null) {
                mPlayerCallBack.onLoad();
            }
            mCurrentState = STATE_PREPARING;
        } catch (IOException e) {
            MyLog.e(TAG, "Unable to open content: " + mUri, e);
            mCurrentState = STATE_ERROR;
            return;
        } catch (IllegalArgumentException e) {
            MyLog.e(TAG, "Unable to open content: " + mUri, e);
            mCurrentState = STATE_ERROR;
            return;
        } catch (Exception e) {
            mCurrentState = STATE_ERROR;
            MyLog.e(e);
            return;
        }

    }

    private void updateGravity() {
        if (mPlayer != null && mViewWidth == 0 && mViewHeight == 0) {
            int layoutWidth = GlobalData.screenWidth;
            int layoutHeight = GlobalData.screenHeight;
            int resizeLayoutHeight = layoutWidth * 16 / 9;
            MyLog.w(TAG, "updateGravity mIsLandscape=" + mIsLandscape);
            if (mIsLandscape) {
                if (mVideoWidth > mVideoHeight && layoutWidth * 16 != layoutHeight * 9) {
                    mPlayer.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFill, resizeLayoutHeight, layoutWidth);
                } else {
                    mPlayer.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit, layoutHeight, layoutWidth);
                }
            } else {
                if (mVideoWidth < mVideoHeight && layoutWidth * 16 != layoutHeight * 9) {
                    mPlayer.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFill, layoutWidth, resizeLayoutHeight);
                } else {
                    mPlayer.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit, layoutWidth, layoutHeight);
                }
            }
        }
    }

    private boolean stopOtherMusic() {
        // 关闭其他音乐
        if (null == mAudioManager) {
            mAudioManager = (AudioManager) GlobalData.app().getSystemService(Context.AUDIO_SERVICE);
        }
        int result = mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            MyLog.w(TAG, "AudioManager result = " + result);
            return false;
        }
        return true;
    }

    private IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
            MyLog.w(TAG, String.format("videoSize : ( %d , %d ) , onVideoSizeChanged: ( %d x %d ) , sarNum / sarDen ( %d / %d ) , player size: ( %d x %d )",
                    mVideoWidth, mVideoHeight, width, height, sarNum, sarDen, mp.getVideoWidth(), mp.getVideoHeight()));
            mVideoWidth = width;
            mVideoHeight = height;
            if (null != mVideoView) {
                mVideoView.adjustVideoLayout(mIsLandscape);
            }
            // 调整旋转方向
            if (width > height) {
                mPlayMode = VideoPlayMode.PLAY_MODE_LANDSCAPE;
                MyLog.d(TAG, "onVideoSizeChanged requestOrientation playMode = " + mPlayMode);
                if (mPlayerCallBack != null) {
                    mPlayerCallBack.requestOrientation(VideoPlayMode.PLAY_MODE_LANDSCAPE);
                }
            } else if (width < height) {
                mPlayMode = VideoPlayMode.PLAY_MODE_PORTRAIT;
                MyLog.d(TAG, "onVideoSizeChanged requestOrientation playMode = " + mPlayMode);
                if (mPlayerCallBack != null) {
                    mPlayerCallBack.requestOrientation(VideoPlayMode.PLAY_MODE_PORTRAIT);
                }
            }
            updateGravity();
        }
    };

    private IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            mCurrentState = STATE_PREPARED;
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            if (mPlayer != null) {
                MyLog.w(TAG, String.format("onPrepared : ( %d x %d )", mPlayer.getVideoWidth(), mPlayer.getVideoHeight()));
                mStartTime = System.currentTimeMillis();
                mIpAddress = mPlayer.getServerAddress();
                mIpStr = "ServerIP: " + mIpAddress + "\n";
                mResolutionStr = "Resolution: " + mPlayer.getVideoWidth() + "x" + mPlayer.getVideoHeight() + "\n";
                mPrepareStr = mIpStr + mResolutionStr + mMediaMetaStr + mFrameStr;
            }
            /** 没有权限进入私密直播时，mVideoView会在{@link #destroyAndChearResource()}被赋值为null */
            if (mVideoView != null) {
                mVideoView.adjustVideoLayout(mIsLandscape);
            }
            updateGravity();
            if (mPlayerCallBack != null) {
                mPlayerCallBack.onPrepared();
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            MyLog.w(TAG, "onCompletion");
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            if (mPlayerCallBack != null) {
                mPlayerCallBack.onCompletion();
            }
        }
    };

    private IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
            MyLog.e(TAG, "framework_err = " + framework_err + " , impl_err = " + impl_err);
            mCurrentState = STATE_ERROR;
            //TODO 播放内核播放失败，需要设置正确的appId,ak,sk
            if (framework_err == -1040) {
                return false;
            }
            if (mPlayerCallBack != null) {
                mPlayerCallBack.onError(framework_err);
            }
            return true;
        }
    };

    private final IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            switch (what) {
                case IMediaPlayer.MEDIA_INFO_RELOADED:
                    MyLog.w(TAG, "MEDIA_INFO_RELOADED");
                    //重新reload后,不会调用onPrepared,所以在这里重置状态
                    mCurrentState = STATE_PREPARED;
                    break;
                default:
                    break;
            }
            if (mPlayerCallBack != null) {
                Message msg = Message.obtain();
                msg.what = what;
                mPlayerCallBack.onInfo(msg);
            }
            return true;
        }
    };

    private final IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            if (mPlayerCallBack != null) {
                mPlayerCallBack.onSeekComplete();
            }
        }
    };

    public boolean hasVideoPlayerCallBack() {
        return mPlayerCallBack != null;
    }

    @Override
    public void setVideoPlayerCallBack(IPlayerCallBack playerCallBack) {
        mPlayerCallBack = playerCallBack;
    }

    public void setGravity(Player.SurfaceGravity gravity, int width, int height) {
        mSurfaceGravity = gravity;
        mViewWidth = width;
        mViewHeight = height;
        if (mPlayer != null) {
            mPlayer.setGravity(gravity, width, height);
        }
    }

    @Override
    public void notifyOrientation(boolean isLandscape) {
        MyLog.w(TAG, "notifyOrientation isLandscape=" + isLandscape);
        if (mIsLandscape != isLandscape && mVideoView != null) {
            mIsLandscape = isLandscape;
            mVideoView.adjustVideoLayout(mIsLandscape);
            updateGravity();
        }
    }

    @Override
    public void resumeTo(long msec) {
        if (null == mPlayer) {
            return;
        }
        MyLog.w(TAG, "seekTo " + msec);
        if (msec >= 0) {
            try {
                mPlayer.seekTo(msec);
            } catch (IllegalStateException e) {
                MyLog.e(e);
            }

        }
    }

    @Override
    public void setIpList(List<String> httpIpList, List<String> localIpList) {
        if (mPlayer != null) {
            MyLog.w(TAG, "setIpList httpIpList=" + httpIpList + ", localIpList=" + localIpList);
            mPlayer.setIpList(httpIpList, localIpList);
        }
    }

    @Override
    public void start() {
        MyLog.w(TAG, "start " + isInPlaybackState() + " , mCurrentState = " + mCurrentState);
        if (isInPlaybackState()) {
            mPlayer.start();
            mCurrentState = STATE_PLAYING;

            if (mPauseStartTime != 0) {
                mPausedTime += System.currentTimeMillis() - mPauseStartTime;
            }
            mPauseStartTime = 0;
        } else if (isInErrorState()) {
            //TODO 是否使用reload
            mPlayer.reset();
            try {
                mPlayer.setDataSource(mUri.toString(), mHost);
                mPlayer.prepareAsync(mRealTime);
                setSurface();
//                        mPlayer.setSurface(mSurfaceHolder.getSurface());
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }
    }

    @Override
    public void reset() {
        if (null != mPlayer) {
            MyLog.w(TAG, "reset");
            //TODO 是否要设置idle状态
            mPlayer.reset();
        }
    }

    @Override
    public void pause() {
        MyLog.w(TAG, "pause");
        if (null == mPlayer) {
            return;
        }
        if (isInPlaybackState()) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            }
            mDuration = mPlayer.getDuration();
            mCurrentState = STATE_PAUSED;

            mPauseStartTime = System.currentTimeMillis();
            stopBitRateSampling();
        }
    }

    @Override
    public void release() {
        final long current = System.currentTimeMillis();
        if (null != mAudioManager) {
            mAudioManager.abandonAudioFocus(null);
        }
        stopBitRateSampling();
        synchronized (MEDIA_PLAYER_LOCK) {
            if (mPlayer != null) {
                mPlayer.reset();
                mPlayer.stop();
                mPlayer.setSurface(null);
                mPlayer.release();
                mPlayer = null;
                mCurrentState = STATE_IDLE;
                if (mPlayerCallBack != null) {
                    mPlayerCallBack.onReleased();
                }
                MyLog.e(TAG, "release cost : " + String.valueOf(System.currentTimeMillis() - current));
            }
        }
    }

    @Override
    public void destroy() {
        // 调用自身的release方法
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    @Override
    public void destroyAndClearResource() {
//        if (mSurface != null) {
//            mSurface.release();
//            mSurface = null;
//        }
//        if (null != mSurfaceTexture) {
//            mSurfaceTexture.release();
//            mSurfaceTexture = null;
//        }
        release();
        mVideoView = null;
        mPlayerCallBack = null;
    }

    @Override
    public String getIpAddress() {
        if (TextUtils.isEmpty(mIpAddress) && mPlayer != null) {
            return mPlayer.getServerAddress();
        } else {
            return mIpAddress;
        }
    }

    @Override
    public long getDuration() {
        if (null == mPlayer) {
            MyLog.w(TAG, "mPlayer is null");
            return -1;
        }
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1l;
        return mDuration;
    }

    private Subscription mBitRateSubscription;

    public void stopBitRateSampling() {
        MyLog.d(TAG, "stopBitRateSampling");
        if (mBitRateSubscription != null && !mBitRateSubscription.isUnsubscribed()) {
            mBitRateSubscription.unsubscribe();
            mBitRateSubscription = null;
        }
    }

    // 获取调试信息
    public String getDebugStr() {
        return mPrepareStr;
    }

    @Override
    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            long position = mPlayer.getCurrentPosition();
            MyLog.w(TAG, "getCurrentPosition : = " + position);
            return position;
        }
        return 0l;
    }

    @Override
    public long getMessageStamp() {
        if (!isInPlaybackState() || null == mPlayer) {
            return 0l;
        }
        return mPlayer.getCurrentStreamPosition();
    }

    @Override
    public long getResumePosition() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public void seekTo(long msec) {
        MyLog.w(TAG, "seekTo " + msec);
        if (isInPlaybackState()) {
            if (msec >= 0) {
                mPlayer.seekTo(msec);
            }
        }
    }

    @Override
    public boolean isPlaying() {
        return mCurrentState == STATE_PREPARED || mCurrentState == STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mCurrentState == STATE_PAUSED;
    }

    private boolean isInPlaybackState() {
        MyLog.w(TAG, "PlaybackState " + mCurrentState);
        return (mPlayer != null && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean isInErrorState() {
        return (mPlayer != null && mCurrentState == STATE_ERROR);
    }

//    /*
//    * 横屏加黑边
//    * */
//    public void setVideoLayout() {
//        if (mVideoHeight > 0 && mVideoWidth > 0) {
//            if (mVideoWidth > mVideoHeight && mIsShowWithBlack) {
//                //如果修改请注意布局为RelativeLayout
//                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
//                float videoRatio = ((float) (mVideoWidth)) / mVideoHeight;
//                MyLog.w(TAG, "videoRatio = " + videoRatio);
//                lp.height = (int) (GlobalData.screenWidth / videoRatio);
//                lp.width = GlobalData.screenWidth;
//                lp.topMargin = DisplayUtils.dip2px(125);
//                lp.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
//                setLayoutParams(lp);
//            } else {
//                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
//                float videoRatio = ((float) (mVideoWidth)) / mVideoHeight;
//                float windowRatio = GlobalData.screenWidth / (float) GlobalData.screenHeight;
//                boolean shouldBeWider = videoRatio > windowRatio;
//                if (shouldBeWider) {
//                    lp.height = GlobalData.screenHeight;
//                    lp.width = (int) (GlobalData.screenHeight * videoRatio);
//                } else {
//                    lp.width = GlobalData.screenWidth;
//                    lp.height = (int) (GlobalData.screenWidth / videoRatio);
//                }
//                MyLog.w(TAG, "videoRatio = " + videoRatio);
//                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
//                lp.topMargin = 0;
//                setLayoutParams(lp);
//            }
//        }
//    }

    public boolean isKsyMediaPlayerNull() {
        return mPlayer == null;
    }

    public void onSurfaceDestroyed() {
        MyLog.d(TAG, "onSurfaceDestroyed");
        if (mPlayer != null) {
            mPlayer.setSurface(null);
        }
        if (null != mSurface) {
            mSurface.release();
            mSurface = null;
        }
        if (null != mSurfaceTexture) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        switch (mInterruptMode) {
            case PlayConfig.INTERRUPT_MODE_RELEASE_CREATE:
                MyLog.d(TAG, "INTERRUPT_MODE_RELEASE_CREATE");
                release();
                break;
            case PlayConfig.INTERRUPT_MODE_PAUSE_RESUME:
                MyLog.d(TAG, "INTERRUPT_MODE_PAUSE_RESUME");
                // 保存上次的暂停状态
                mLastStopped = (mCurrentState != STATE_PLAYING);
                pause();
                break;
            case PlayConfig.INTERRUPT_MODE_FINISH_OR_ERROR:
                MyLog.d(TAG, "INTERRUPT_MODE_FINISH_OR_ERROR");
                break;
        }
    }

    public void onSurfaceAvailable(Surface surface) {
        if (null != mSurface) {
            mSurface.release();
        }
        mSurface = surface;
        switch (mInterruptMode) {
            case PlayConfig.INTERRUPT_MODE_RELEASE_CREATE:
                MyLog.d(TAG, "INTERRUPT_MODE_RELEASE_CREATE");
                //TODO notice this
                if (mIsNeedReset) {
                    openVideo();
                }
                break;
            case PlayConfig.INTERRUPT_MODE_PAUSE_RESUME:
                MyLog.d(TAG, "INTERRUPT_MODE_PAUSE_RESUME mKsyMediaPlayer is null = "
                        + (isKsyMediaPlayerNull()) + " , mLastStopped = " + mLastStopped);
                if (!isKsyMediaPlayerNull()) {
                    mPlayer.setSurface(mSurface);
                    // 如果上次不是暂停状态就继续播放
                    if (!mLastStopped) {
                        start();
                    }
                } else {
                    openVideo();
                }
                break;
            case PlayConfig.INTERRUPT_MODE_FINISH_OR_ERROR:
                MyLog.d(TAG, "INTERRUPT_MODE_FINISH_OR_ERROR");
                if (mPlayer != null) {
                    mPlayer.setSurface(mSurface);
                }
                break;
            case INTERRUPT_MODE_DEFAULT:
                MyLog.w(TAG, "INTERRUPT_MODE_DEFAULT");
                if (mPlayer != null) {
                    mPlayer.setSurface(mSurface);
                    // 不是wifi网络,同时上次暂停时是播放状态的话,重新start player
//                    if (!mWifiNetwork && !mLastStopped) {
//                        start();
//                    }
                } else {
                    openVideo();
                }
                break;
            case PlayConfig.INTERRUPT_MODE_ESPORT_PAUSE_RESUME:
                MyLog.w(TAG, "INTERRUPT_MODE_ESPORT_PAUSE_RESUME");
                openVideo();
                MyLog.e("mVideoWidth=" + mVideoWidth + ",mVideoHeight=" + mVideoHeight);
                setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspect, mVideoWidth, mVideoHeight);
                break;
        }
    }

    public void onSurfaceChanged(Surface surface) {
        mSurface = surface;
        if (mPlayer != null) {
            mPlayer.setSurface(mSurface);
        }
    }

    public void onSurfaceTextureChanged(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
        setSurface();
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture) {
        if (null != mSurface) {
            mSurface.release();
            mSurface = null;
        }
        mSurfaceTexture = surfaceTexture;
        if (mVideoView == null) {
            return;
        }
        switch (mInterruptMode) {
            case PlayConfig.INTERRUPT_MODE_RELEASE_CREATE:
                MyLog.d(TAG, "INTERRUPT_MODE_RELEASE_CREATE");
                //TODO notice this
                if (mIsNeedReset) {
                    openVideo();
                }
                break;
            case PlayConfig.INTERRUPT_MODE_PAUSE_RESUME:
                MyLog.d(TAG, "INTERRUPT_MODE_PAUSE_RESUME mPlayer is null = "
                        + (isKsyMediaPlayerNull()) + " , mLastStopped = " + mLastStopped);
                if (!isKsyMediaPlayerNull()) {
                    setSurface();
                    // 如果上次不是暂停状态就继续播放
                    if (!mLastStopped) {
                        start();
                    }
                } else {
                    openVideo();
                }
                break;
            case PlayConfig.INTERRUPT_MODE_FINISH_OR_ERROR:
                MyLog.d(TAG, "INTERRUPT_MODE_FINISH_OR_ERROR");
                setSurface();
                break;
            case INTERRUPT_MODE_DEFAULT:
                MyLog.w(TAG, "INTERRUPT_MODE_DEFAULT");
                if (mPlayer != null) {
                    setSurface();
                } else {
                    openVideo();
                }
                break;
            case PlayConfig.INTERRUPT_MODE_ESPORT_PAUSE_RESUME:
                MyLog.w(TAG, "INTERRUPT_MODE_ESPORT_PAUSE_RESUME");
                openVideo();
                setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspect, mVideoWidth, mVideoHeight);
                break;
        }
    }

    public int getVideoWidth() {
        if (mPlayer != null) {
            return mPlayer.getVideoWidth();
        }
        return 0;
    }

    public int getVideoHeight() {
        if (mPlayer != null) {
            return mPlayer.getVideoHeight();
        }
        return 0;
    }

    public void shiftUp(float ratio) {
        if (mPlayer != null) {
            mPlayer.shiftUp(ratio);
        }
    }

    @Override
    public void setVolume(float var1, float var2) {
        mVolumeL = var1;
        mVolumeR = var2;
        if (mPlayer != null) {
            mPlayer.setVolume(mVolumeL, mVolumeR);
        }
    }

    @Override
    public void setBufferSize(int size) {
        mBufferSize = size;
        if (mPlayer != null) {
            mPlayer.setBufferSize(mBufferSize);
        }
    }

    public long getStreamId() {
        if (mPlayer != null) {
            return mPlayer.getStreamId();
        }
        return 0;
    }

    public long getAudioSource() {
        if (mPlayer != null) {
            return mPlayer.getAudioSource();
        }
        return 0;
    }

    public void setLooping(boolean looping) {
        mLooping = looping;
        if (mPlayer != null) {
            mPlayer.setLooping(mLooping);
        }
    }

    public interface OnReportBitRateListener {
        void onReportBitRate(String rate);
    }

    public void setRealTime(boolean realTime) {
        mRealTime = realTime;
    }

    public void setTransferObserver(long observer) {
        mTransferObserver = observer;
    }

    public void setPlayerMode(PlayerWorkingMode mode) {
        mPlayerMode = mode;
    }
}
