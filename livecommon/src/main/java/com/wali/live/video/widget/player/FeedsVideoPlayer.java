package com.wali.live.video.widget.player;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.toast.ToastUtils;
import com.live.module.common.R;
import com.mi.live.engine.media.player.IMediaPlayer;
import com.wali.live.dns.IStreamReconnect;
import com.wali.live.event.EventClass;
import com.wali.live.ipselect.BaseIpSelectionHelper;
import com.wali.live.ipselect.FeedsIpSelectionHelper;
import com.wali.live.receiver.NetworkReceiver;
import com.wali.live.video.widget.IPlayerCallBack;
import com.wali.live.video.widget.VideoPlayMode;
import com.wali.live.video.widget.VideoPlayerPresenter;
import com.wali.live.video.widget.VideoPlayerTextureView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

/**
 * 播放器
 * Created by yurui on 16-10-18.
 *
 * @module feeds
 */
@Deprecated
public class FeedsVideoPlayer extends RelativeLayout implements IPlayerCallBack,
        VideoPlayBaseSeekBar.VideoPlaySeekBarListener, IStreamReconnect, View.OnClickListener {

    protected final static String TAG = "FeedsVideoPlayer";

    protected static final int CLICK_TAG_ROTATE = 1001;

    public FeedsVideoPlayer(Context context) {
        this(context, null);
    }

    public FeedsVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        onCreate();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    //-------view-----------------------------------------------------------------------------------
    protected VideoPlayerTextureView mVideoView;
    protected View mLoadingView;
    protected RelativeLayout mSeekBarContainer;
    protected ImageView mRotateBtn;

    protected VideoPlayBaseSeekBar mSeekBar;
    protected VideoPlayerPresenter mVideoPlayerPresenter;
    protected Animation mAnime;
    protected Context mContext;
    protected Handler mHandler = new MyUIHandler(this);


    //-------data-----------------------------------------------------------------------------------
    protected String mLiveId;
    protected String mHost;
    protected boolean mIsActivate = false;
    protected boolean mIsAlreadyPrepared = false;
    protected boolean mIsCompletion = false;
    protected boolean mIsFullScreen = false;
    protected boolean mIsPlaying = false;
    protected boolean mIsShowSeekBar = true;
    protected boolean mIsSeekBarEnable = true;    //是否允许出现进度条
    protected boolean mSeekBarUserTouch = false;
    protected boolean mSoundsEnable = true;
    protected boolean mIsPlayLocal = false;       //是否正在播放本地
    protected boolean mIsLandscape = false;       //播放器view是否横屏

    protected int mEventId = 0;
    protected long mPlayedTime = 0;
    protected long mTotalTime = 0;
    protected long mPreSeekTo = 0;
    protected long mSeekBarHideDelay = 0;
    protected int mTransMode = VideoPlayerTextureView.TRANS_MODE_CENTER_CROP;//VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE


    protected TempForceOrientListener mTempForceOrientListener;

    // 域名解析、重连相关
    public static final int MSG_RELOAD_VIDEO = 100;  // onInfo开始buffer时，reload数据的标记。
    private int mBufferingCount = 0;
    protected BaseIpSelectionHelper mIpSelectionHelper;


    //-------需要外部调用的生命周期相关api--------------------------------------------------------------

    protected void onCreate() {
        createView();
        mIpSelectionHelper = new FeedsIpSelectionHelper(mContext, this);//, KeyFlowReportManager.INSTANCE
        mVideoView = $(R.id.video_view);
        mLoadingView = $(R.id.loading_iv);
        mSeekBarContainer = $(R.id.seek_bar_container);
        mRotateBtn = $(R.id.rotate_btn);
        mRotateBtn.setSelected(false);
        mRotateBtn.setVisibility(View.GONE);
        mRotateBtn.setOnClickListener(this);
        mRotateBtn.setTag(CLICK_TAG_ROTATE);

        mSeekBarContainer.setOnTouchListener(mSeekBarOnTouchListener);
        mVideoPlayerPresenter = mVideoView.getVideoPlayerPresenter();
        mVideoPlayerPresenter.setVideoPlayerCallBack(this);
        mVideoPlayerPresenter.setBufferSize(500);
        mVideoPlayerPresenter.setRealTime(false);
        mAnime = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.ml_loading_animation);
        mPlayedTime = 0;
        mTotalTime = 0;
    }

    //销毁时候调用
    public void onDestroy() {
        stop();
        if (mIpSelectionHelper != null) {
            mIpSelectionHelper.destroy();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    protected View createView() {
        return inflate(mContext, R.layout.video_play_fragment, this);
    }


    //------控制流程 如 播放 停止 暂停 继续等 seekto-----------------------------------------------------------
    //从头开始播放视频
    public void play(String videoUrl) {//, ViewGroup viewGroup, boolean isCinemaMode, int transMode, boolean soundOn, boolean showSeekBar
        if (!TextUtils.isEmpty(videoUrl)) {
            mPreSeekTo = 0;
            mIsAlreadyPrepared = false;
            setSeekBarContainerVisible(false);
            showLoading();
            //ip优选
            mIpSelectionHelper.setOriginalStreamUrl(videoUrl);
            mIpSelectionHelper.ipSelect();
            if (TextUtils.isEmpty(mLiveId)) {
                mVideoPlayerPresenter.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
            } else {
                mVideoPlayerPresenter.setVideoPath(mLiveId, mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
            }
            mVideoPlayerPresenter.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());

            MyLog.w(TAG," mIpSelectionHelper.getStreamUrl() = " + mIpSelectionHelper.getStreamUrl());
            MyLog.w(TAG," mIpSelectionHelper.getSelectedHttpIpList() = " + mIpSelectionHelper.getSelectedHttpIpList());
            MyLog.w(TAG," mIpSelectionHelper.getSelectedLocalIpList() = " + mIpSelectionHelper.getSelectedLocalIpList());
            mVideoPlayerPresenter.setVideoPlayerCallBack(this);
            mVideoPlayerPresenter.setVolume(mSoundsEnable ? 1 : 0, mSoundsEnable ? 1 : 0);
            setTransMode(mTransMode);
            mPlayedTime = 0;
            mTotalTime = 0;
            if (mSeekBar != null) {
                mSeekBar.setProgress(mPlayedTime, mTotalTime, true);
            }
            resume();
        }
        mIsActivate = true;
        mHandler.postDelayed(mOnSeekProgressRunnable, 500);
    }

    //继续播放
    public void resume() {
        if (mIsCompletion) {
            mIsCompletion = false;
            mVideoPlayerPresenter.seekTo(0);
        }
        mVideoPlayerPresenter.start();
        setPlayBtnSelected(true);
        EventBus.getDefault().post(new EventClass.FeedsVideoEvent(false, EventClass.FeedsVideoEvent.TYPE_START));
    }

    //暂停播放
    public void pause() {
        mVideoPlayerPresenter.pause();
        setPlayBtnSelected(false);
        EventBus.getDefault().post(new EventClass.FeedsVideoEvent(false, EventClass.FeedsVideoEvent.TYPE_STOP));
    }

    //停止播放
    public void stop() {
        mVideoPlayerPresenter.release();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        hideLoading();
        //TODO remove
        setPlayBtnSelected(false);
        mIsActivate = false;
    }

    //如果没有prepare会记录 可以预seekto
    public void seekTo(long time) {
        if (time < 0) {
            time = 0;
        }
        mPreSeekTo = time;
        if (mIsAlreadyPrepared) {
            if (mPreSeekTo > 0) {
                mVideoPlayerPresenter.seekTo(mPreSeekTo);
                mPreSeekTo = 0;
            }
        }
    }

    public void destroy() {
        onDestroy();
    }

    private void reconnect() {
        long resumeTime = mVideoPlayerPresenter.getResumePosition();
        MyLog.w(TAG, "reconnect, resumeTime= " + resumeTime);
        mVideoPlayerPresenter.resumeTo(resumeTime);
    }

    public void orientLandscape() {
        mIsLandscape = true;
        showPortraitRotateIfNeed();
    }

    public void orientPortrait() {
        mIsLandscape = false;
        showPortraitRotateIfNeed();
    }

    //------set & get-------------------------------------------------------------------------------

    public void setRealTime(boolean realTime) {
        if (mVideoPlayerPresenter != null) {
            mVideoPlayerPresenter.setRealTime(false);
        }
    }

    // 设置 event的id 防止不必要的事件响应
    public void setEventId(int id) {
        mEventId = id;
    }

    public void setLiveId(String liveId) {
        mLiveId = liveId;
    }

    public void setHost(String host) {
        mHost = host;
    }

    public void setTempForceOrientListener(TempForceOrientListener listener) {
        mTempForceOrientListener = listener;
    }

    //设置播放模式  VideoPlayerTextureView.TRANS_MODE_CENTER_CROP | VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE
    public void setTransMode(int transMode) {
        if (mTransMode != transMode) {
            mTransMode = transMode;
            mVideoView.setVideoTransMode(mTransMode);
        }
    }

    //判断是不是衡平流
    public boolean isStreamLandscape() {
        return mVideoPlayerPresenter.getPlayMode() == VideoPlayMode.PLAY_MODE_LANDSCAPE;
    }

    //设置seekbar 消失的时长 小于等于0 不消失
    public void setSeekBarHideDelay(long delay) {
        mSeekBarHideDelay = delay;
    }

    //设置进度条区域全屏按钮可见
    public void setSeekBarFullScreenBtnVisible(boolean visible) {
        if (mSeekBar != null) {
            mSeekBar.showOrHideFullScreenBtn(visible);
        }
    }

    //设置进度条区域播放按钮可见
    public void setSeekBarPlayBtnVisible(boolean isShow) {
        if (mSeekBar != null) {
            mSeekBar.showOrHidePlayBtn(isShow);
        }
    }

    //设置进度条
    public void setVideoPlayBaseSeekBar(VideoPlayBaseSeekBar seekBar) {
        if (mSeekBarContainer != null && mSeekBarContainer.getChildCount() > 0) {
            if (mSeekBar != null) {
                mSeekBarContainer.removeView(mSeekBar);
            }
        }
        mSeekBar = seekBar;
        if (mSeekBar != null) {
            if (mSeekBarContainer != null) {
                mSeekBarContainer.addView(mSeekBar);
                mSeekBar.setVideoPlaySeekBarListener(this);
                mSeekBar.setPlayBtnSelected(isPlaying());
                mSeekBarContainer.requestLayout();
                delayHideSeekBar(mSeekBarHideDelay);
            }
        }
    }

    public void setSeekBarEnable(boolean enable) {
        mIsSeekBarEnable = enable;
    }

    public void setSeekBarContainerVisible(boolean visible) {
        visible = visible && mIsSeekBarEnable;
        if (mSeekBarHideDelay > 0) {
            mHandler.removeCallbacks(mHideSeekBarRunnable);
        }
        if (mSeekBar != null) {
            mSeekBar.setVisibility(visible && mIsAlreadyPrepared ? View.VISIBLE : View.GONE);
        }
        if (visible && mIsAlreadyPrepared) {
            delayHideSeekBar(mSeekBarHideDelay);
        }
    }

    public boolean isSeekBarContainerVisible() {
        return (mSeekBar != null && mSeekBar.getVisibility() == View.VISIBLE);
    }

    //设置标记 是否全屏状态
    public void setFullScreen(boolean fullScreen) {
        mIsFullScreen = fullScreen;
    }

    //是否全屏状态
    public boolean isFullScreen() {
        return mIsFullScreen;
    }

    //判断是否处于激活状态  调用play之后没有destory
    public boolean isActivate() {
        return mIsActivate;
    }

    //判断是否处于播放状态 与播放按钮状态同步
    public boolean isPlaying() {
        return mIsPlaying;
    }

    public RelativeLayout getmSeekBarContainer(){
        return mSeekBarContainer;
    }

    //播放进度
    public long getCurrentPosition() {
        if (mVideoPlayerPresenter != null) {
            return mVideoPlayerPresenter.getCurrentPosition();
        }
        return 0l;
    }

    public long getResumePosition() {
        if (mVideoPlayerPresenter != null) {
            return mVideoPlayerPresenter.getResumePosition();
        }
        return 0l;
    }

    public boolean isDisplayLandscape() {
        return false;
    }


    //---------内部调 用方法--------------------------------------------------------------------------
    //改变seekbar 播放按钮图标
    protected void setPlayBtnSelected(boolean playing) {
        if (mSeekBar != null) {
            mSeekBar.setPlayBtnSelected(playing);
        }
        mIsPlaying = playing;
    }

    //延迟隐藏seekbar区域
    protected void delayHideSeekBar(long delay) {
        if (delay > 0 && mHandler != null) {
            mHandler.removeCallbacks(mHideSeekBarRunnable);
            mHandler.postDelayed(mHideSeekBarRunnable, delay);
        }
    }

    protected void showLoading() {
        // 去掉loading
//        if (mLoadingView != null && mAnime != null) {
//            mLoadingView.setVisibility(View.VISIBLE);
//            mLoadingView.startAnimation(mAnime);
//        }
    }

    protected void hideLoading() {
        if (mLoadingView != null) {
            mLoadingView.clearAnimation();
            mLoadingView.setVisibility(View.GONE);
        }
    }

    /*
    * 竖屏 竖屏流 不显示  select = false
    * 竖屏 横屏流 显示   select = false
    * 横屏 竖屏流 不显示  select = true
    * 横屏 横屏流 显示   select = true
    */
    protected void showPortraitRotateIfNeed() {
        if (mVideoPlayerPresenter != null) {
            if (!mIsLandscape) {
                //竖屏
                if (mVideoPlayerPresenter.getPlayMode() == VideoPlayMode.PLAY_MODE_LANDSCAPE) {
                    mRotateBtn.setVisibility(View.VISIBLE);
                } else {
                    mRotateBtn.setVisibility(View.GONE);
                }
                mRotateBtn.setSelected(false);
            } else {
                //横屏
                if (mVideoPlayerPresenter.getPlayMode() == VideoPlayMode.PLAY_MODE_LANDSCAPE) {
                    mRotateBtn.setVisibility(View.VISIBLE);
                } else {
                    mRotateBtn.setVisibility(View.GONE);
                }
                mRotateBtn.setSelected(true);
            }
        } else {
            mRotateBtn.setVisibility(View.GONE);
            mRotateBtn.setSelected(false);
        }
    }

    public void clearTouchListener() {
        mSeekBarOnTouchListener = null;
        mSeekBarContainer.setOnTouchListener(null);
    }

    //------用Touch模拟点击事件---------------------------------------------------------------------------------
    protected OnTouchListener mSeekBarOnTouchListener = new OnTouchListener() {
        protected float mLastX = -1, mLastY = -1;
        protected boolean mIsClick = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mLastX = event.getRawX();
                    mLastY = event.getRawY();
                    mIsClick = true;
                }
                break;
                case MotionEvent.ACTION_MOVE: {
                    float x = event.getRawX();
                    float y = event.getRawY();
                    if (Math.sqrt((x - mLastX) * (x - mLastX) + (y - mLastY) * (y - mLastY)) > 30) {
                        mIsClick = false;
                    }
                }
                break;
                case MotionEvent.ACTION_CANCEL: {
                    mIsClick = false;
                }
                break;
                case MotionEvent.ACTION_UP: {
                    float x = event.getRawX();
                    float y = event.getRawY();
                    if (mIsClick && Math.sqrt((x - mLastX) * (x - mLastX) + (y - mLastY) * (y - mLastY)) <= 30) {
                        onSeekBarContainerClick();
                    }
                    mIsClick = false;
                }
                break;
            }
            return mIsClick;
        }
    };

    public void onSeekBarContainerClick() {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
//        switch (view.getId()) {
//            case R.id.seek_bar_container:
//
//                break;
//        }
        if (isSeekBarContainerVisible()) {
            setSeekBarContainerVisible(false);
        } else {
            setSeekBarContainerVisible(true);
        }
    }

    //网络变化toast提示
    protected void wifiTo4g() {
        if (isActivate()) {
            ToastUtils.showToast(R.string.feeds_detail_wifi_2_4g_hint);
        }
    }


    //------Runnable--------------------------------------------------------------------------------
    //更新进度条播放进度的Runnable
    Runnable mOnSeekProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mVideoPlayerPresenter != null) {
                if (!mSeekBarUserTouch && isPlaying()) {
                    mPlayedTime = mVideoPlayerPresenter.getCurrentPosition();
                    mTotalTime = mVideoPlayerPresenter.getDuration();
                    if (mPlayedTime > mTotalTime) {
                        mPlayedTime = mTotalTime;
                    }
                    if (mSeekBar != null) {
                        mSeekBar.setProgress(mPlayedTime, mTotalTime, true);
                    }
                }
                if (mIsActivate) {
                    mHandler.postDelayed(mOnSeekProgressRunnable, 500);
                }
            }
        }
    };

    //延迟隐藏seekbar的Runnable
    Runnable mHideSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            setSeekBarContainerVisible(false);
        }
    };


    //--------接口回调的代码--------------------------------------------------------------------------
    //    ----接口回调的代码     SeekBar回调代码-------------------------------------------------------
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mTotalTime > 0 && fromUser) {
            mPlayedTime = (long) (mTotalTime * progress * 1.0 / mSeekBar.getMax());
            if (mSeekBar != null) {
                mSeekBar.setProgress(mPlayedTime, mTotalTime, false);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mSeekBarUserTouch = true;
        if (mSeekBarHideDelay > 0) {
            mHandler.removeCallbacks(mHideSeekBarRunnable);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        showLoading();
        if (mVideoPlayerPresenter != null) {
            if (mPlayedTime < 0) {
                mPlayedTime = 0;
            }

            if (!mVideoPlayerPresenter.isPlaying()) {
                mVideoPlayerPresenter.start();
                setPlayBtnSelected(true);
            }
            mVideoPlayerPresenter.seekTo(mPlayedTime);
            MyLog.v(TAG, "onStopTrackingTouch " + mVideoPlayerPresenter.isPlaying() + " " + mVideoPlayerPresenter.isPaused());
            EventBus.getDefault().post(new EventClass.FeedsVideoEvent(false, EventClass.FeedsVideoEvent.TYPE_START));
        }
        mSeekBarUserTouch = false;
        delayHideSeekBar(mSeekBarHideDelay);
    }

    @Override
    public void onClickPlayBtn() {
        //这个是进度条上的播放按钮 视频加在完成后才能用 作用暂停和继续播放
        delayHideSeekBar(mSeekBarHideDelay);
        if (CommonUtils.isFastDoubleClick() || !mIsAlreadyPrepared) {
            return;
        }
        if (mVideoPlayerPresenter.isPlaying()) {
            pause();
        } else {
            resume();
        }
    }

    @Override
    public void onClickFullScreenBtn() {
        delayHideSeekBar(mSeekBarHideDelay);
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        EventBus.getDefault().post(new EventClass.FeedsVideoEvent(false, EventClass.FeedsVideoEvent.TYPE_FULLSCREEN));
    }


    //    ----接口回调的代码     视频播放回调-----------------------------------------------------------
    @Override
    public void onLoad() {
        MyLog.v(TAG, " onLoad");
    }

    @Override
    public void onPrepared() {
        MyLog.v(TAG, " onPrepared");
        if (mVideoPlayerPresenter != null) {
            if (mIsPlayLocal) {
                //本地视频onPrepared 就能播放了 而网络视频要oninfo的时候才能开播
                hideLoading();
                EventBus.getDefault().post(new EventClass.FeedsVideoEvent(false, EventClass.FeedsVideoEvent.TYPE_PLAYING));
            }

            mIsAlreadyPrepared = true;

            mPlayedTime = 0;
            mTotalTime = mVideoPlayerPresenter.getDuration();
            if (mPreSeekTo > 0) {
                mVideoPlayerPresenter.seekTo(mPreSeekTo);
                mPreSeekTo = 0;
            }
        }
    }

    @Override
    public void onCompletion() {
        MyLog.v(TAG, " onCompletion");
        mIsCompletion = true;
        mPlayedTime = mTotalTime;
        if (mSeekBar != null) {
            mSeekBar.setProgress(mPlayedTime, mTotalTime, true);
        }
        setPlayBtnSelected(false);
        EventBus.getDefault().post(new EventClass.FeedsVideoEvent(false, EventClass.FeedsVideoEvent.TYPE_COMPLETION));
    }

    @Override
    public void onError(int errCode) {
        MyLog.v(TAG, " onError " + errCode);
        pause();
    }

    @Override
    public void onBufferingUpdate(int percent) {
        MyLog.v(TAG, " onBufferingUpdate " + percent);
    }

    @Override
    public void onInfo(int info) {
        MyLog.v(TAG + " onInfo int " + info);
        if (info == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START || info == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
            hideLoading();
            mIsAlreadyPrepared = true;
            setSeekBarContainerVisible(mIsShowSeekBar);
            EventBus.getDefault().post(new EventClass.FeedsVideoEvent(false, EventClass.FeedsVideoEvent.TYPE_PLAYING));
        }
    }

    @Override
    public void onInfo(Message msg) {
        onInfo(msg.what);
        switch (msg.what) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                MyLog.w(TAG, "MEDIA_INFO_BUFFERING_START");
                if (mIpSelectionHelper != null) {
                    if (!mIpSelectionHelper.isStuttering()) {
                        showLoading();
                    }
                    mIpSelectionHelper.updateStutterStatus(true);
                }
                mHandler.removeMessages(MSG_RELOAD_VIDEO);
                mHandler.sendEmptyMessageDelayed(MSG_RELOAD_VIDEO, com.base.utils.Constants.PLAYER_KADUN_RELOAD_TIME);
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                MyLog.w(TAG, "MEDIA_INFO_BUFFERING_END");
                if (mIpSelectionHelper != null) {
                    if (mIpSelectionHelper.isStuttering()) {
                        hideLoading();
                    }
                    mIpSelectionHelper.updateStutterStatus(false);
                }
                mHandler.removeMessages(MSG_RELOAD_VIDEO);
                break;
            default:
                break;
        }
        MyLog.v(TAG, " onInfo " + msg.toString());
    }

    @Override
    public void onSeekComplete() {
        MyLog.v(TAG, " onSeekComplete");
    }

    @Override
    public void requestOrientation(int playMode) {
        MyLog.v(TAG, " playMode " + playMode);
        showPortraitRotateIfNeed();
    }

    @Override
    public void onReleased() {
        MyLog.v(TAG, " onReleased");
    }


    //    ----接口回调的代码     ip优选回调-----------------------------------------------------------
    @Override
    public void onDnsReady() {
        MyLog.w(TAG, "onDnsReady");
        if (mIpSelectionHelper.isStuttering()) {
            startReconnect(0);
        }
    }

    @Override
    public boolean ipSelect() {
        return mIpSelectionHelper.ipSelect();
    }

    @Override
    public void startReconnect(int code) {
        mBufferingCount++;
        MyLog.w(TAG, "startReconnect, mBufferingCount=" + mBufferingCount);
        mHandler.removeMessages(MSG_RELOAD_VIDEO);
        if (mIpSelectionHelper != null) {
            ipSelect();
            mVideoPlayerPresenter.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
            mVideoPlayerPresenter.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());
        }
        reconnect();
    }

    //    ----点击事件-----------------------------------------------------------
    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick() || !(v.getTag() instanceof Integer)) {
            return;
        }
        switch ((int) v.getTag()) {
            case CLICK_TAG_ROTATE:
                // 点击了转屏幕按钮
                EventClass.FeedsVideoEvent event = new EventClass.FeedsVideoEvent(false, EventClass.FeedsVideoEvent.TYPE_ON_CLICK_ROTATE);
                EventBus.getDefault().post(event);
                break;
        }
    }

    //---------内部类--------------------------------------------------------------------------------
    public static interface TempForceOrientListener {
        void onTempForceLandscape();

        void onTempForcePortrait();
    }

    private static class MyUIHandler extends Handler {

        private final WeakReference<FeedsVideoPlayer> mFeedsVideoPlayer;

        public MyUIHandler(FeedsVideoPlayer feedsVideoPlayer) {
            super(Looper.getMainLooper());
            mFeedsVideoPlayer = new WeakReference<>(feedsVideoPlayer);
        }

        @Override
        public void handleMessage(Message msg) {
            FeedsVideoPlayer feedsVideoPlayer = mFeedsVideoPlayer.get();
            if (feedsVideoPlayer == null) {
                return;
            }
            switch (msg.what) {
                case MSG_RELOAD_VIDEO:
                    MyLog.w(TAG, "MSG_RELOAD_VIDEO");
                    feedsVideoPlayer.startReconnect(0);
                    break;
                default:
                    break;
            }
        }
    }


    //---------接受event bus-------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.ReleasePlayerEvent event) {
        //getActivity().finish();
    }

    //网络变化toast提示
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.NetWorkChangeEvent event) {
        if (null != event) {
            NetworkReceiver.NetState netCode = event.getNetState();
            if (netCode != NetworkReceiver.NetState.NET_NO) {
                //优先处理错误情况
                if (netCode == NetworkReceiver.NetState.NET_2G ||
                        netCode == NetworkReceiver.NetState.NET_3G ||
                        netCode == NetworkReceiver.NetState.NET_4G) {
                    wifiTo4g();
                }
            }
        }
    }

    // 不想使用ButterKnife，可以使用下面方法简化代码
    public <V extends View> V $(int id) {
        return (V) findViewById(id);
    }
}
