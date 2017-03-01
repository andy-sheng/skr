package com.wali.live.watchsdk.watch.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.base.event.SdkEventClass;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.engine.media.player.IMediaPlayer;
import com.mi.live.engine.player.widget.VideoPlayMode;
import com.mi.live.engine.player.widget.VideoPlayerCallBackWrapper;
import com.mi.live.engine.player.widget.VideoPlayerPresenter;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;
import com.wali.live.dns.IStreamReconnect;
import com.wali.live.event.EventClass;
import com.wali.live.ipselect.BaseIpSelectionHelper;
import com.wali.live.ipselect.FeedsIpSelectionHelper;
import com.wali.live.receiver.NetworkReceiver;
import com.wali.live.video.widget.player.ReplaySeekBar;
import com.wali.live.video.widget.player.VideoPlayBaseSeekBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

/**
 * Created by chengsimin on 2016/12/23.
 */

public class VideoPlayerPresenterEx implements
        IStreamReconnect, View.OnClickListener {
    protected final static String TAG = "FeedsVideoPlayer";

    protected static final int CLICK_TAG_ROTATE = 1001;

    public VideoPlayerPresenterEx(Context context, VideoPlayerTextureView mVideoView, ReplaySeekBar seekBar, ImageView rotateBtn, boolean realTime) {
        mContext = context;
        this.mVideoView = mVideoView;
        this.mSeekBar = seekBar;
        this.mRotateBtn = rotateBtn;
        onCreate(realTime);
    }

    //-------view-----------------------------------------------------------------------------------
    protected VideoPlayerTextureView mVideoView; // 真正的播放view
    protected ReplaySeekBar mSeekBar; // 真正的进度条
    protected ImageView mRotateBtn;
    protected VideoPlayerPresenter mVideoPlayerPresenter;
    protected Animation mAnime;
    protected Context mContext;
    protected Handler mHandler = new MyUIHandler(this);


    //-------data-----------------------------------------------------------------------------------
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


    // 域名解析、重连相关
    public static final int MSG_RELOAD_VIDEO = 100;  // onInfo开始buffer时，reload数据的标记。
    private int mBufferingCount = 0;
    protected BaseIpSelectionHelper mIpSelectionHelper;

    VideoPlayerCallBackWrapper mIPlayerCallBack = new VideoPlayerCallBackWrapper() {

        @Override
        public void onPrepared() {
            MyLog.v(TAG, " onPrepared");
            if (mVideoPlayerPresenter != null) {
                hideLoading();
                mIsAlreadyPrepared = true;
                setSeekBarContainerVisible(true);
                EventBus.getDefault().post(new EventClass.FeedsVideoEvent(false, EventClass.FeedsVideoEvent.TYPE_PLAYING));
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
        public void onInfo(int info) {
            MyLog.v(TAG + " onInfo int " + info);
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
        public void requestOrientation(int playMode) {
            showPortraitRotateIfNeed();
        }
    };

    VideoPlayBaseSeekBar.VideoPlaySeekBarListener mVideoPlaySeekBarListener = new VideoPlayBaseSeekBar.VideoPlaySeekBarListener() {
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
                MyLog.v(TAG, "onStopTrackingTouch mPlayedTime：" + mPlayedTime);
                EventBus.getDefault().post(new EventClass.FeedsVideoEvent(false, EventClass.FeedsVideoEvent.TYPE_START));
            }
            mSeekBarUserTouch = false;
            delayHideSeekBar(mSeekBarHideDelay);
        }
    };
    //-------需要外部调用的生命周期相关api--------------------------------------------------------------

    protected void onCreate(boolean realTime) {
        mIpSelectionHelper = new FeedsIpSelectionHelper(mContext, this);//, KeyFlowReportManager.INSTANCE

        mVideoPlayerPresenter = mVideoView.getVideoPlayerPresenter();
        mVideoPlayerPresenter.setRealTime(realTime);
        mVideoPlayerPresenter.setVideoPlayerCallBack(mIPlayerCallBack);
        mVideoPlayerPresenter.setBufferSize(500);
        mVideoPlayerPresenter.setLogInfo(UserAccountManager.getInstance().getUuid(), MiLinkClientAdapter.getsInstance().getClientIp());
        if (mSeekBar != null) {
            mSeekBar.setVideoPlaySeekBarListener(mVideoPlaySeekBarListener);
            mSeekBar.setPlayBtnSelected(isPlaying());
            delayHideSeekBar(mSeekBarHideDelay);
        }
        mAnime = AnimationUtils.loadAnimation(GlobalData.app(), com.live.module.common.R.anim.ml_loading_animation);
        mPlayedTime = 0;
        mTotalTime = 0;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
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
            mVideoPlayerPresenter.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
            mVideoPlayerPresenter.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());

            MyLog.w(TAG, " mIpSelectionHelper.getStreamUrl() = " + mIpSelectionHelper.getStreamUrl());
            MyLog.w(TAG, " mIpSelectionHelper.getSelectedHttpIpList() = " + mIpSelectionHelper.getSelectedHttpIpList());
            MyLog.w(TAG, " mIpSelectionHelper.getSelectedLocalIpList() = " + mIpSelectionHelper.getSelectedLocalIpList());
            mVideoPlayerPresenter.setVideoPlayerCallBack(mIPlayerCallBack);
            mVideoPlayerPresenter.setVolume(mSoundsEnable ? 1 : 0, mSoundsEnable ? 1 : 0);
            setTransMode(mTransMode);
            mPlayedTime = 0;
            mTotalTime = 0;
            if (mSeekBar != null) {
                mSeekBar.setProgress(mPlayedTime, mTotalTime, true);
                mHandler.postDelayed(mOnSeekProgressRunnable, 500);
            }
            resume();
        }
        mIsActivate = true;
    }

    private void showLoading() {

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

    private void hideLoading() {

    }

    public void destroy() {
        onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void reconnect() {
        long resumeTime = mVideoPlayerPresenter.getResumePosition();
        MyLog.w(TAG, "reconnect, resumeTime= " + resumeTime);
        mVideoPlayerPresenter.resumeTo(resumeTime);
    }

    @Subscribe
    public void onEvent(SdkEventClass.OrientEvent event) {
        if (event.isLandscape()) {
            orientLandscape();
        } else {
            orientPortrait();
        }
    }

    private void orientLandscape() {
        mIsLandscape = true;
        orientRotateBtn();
    }

    private void orientPortrait() {
        mIsLandscape = false;
        orientRotateBtn();
    }

    protected void orientRotateBtn() {
        showPortraitRotateIfNeed();
        if (mIsLandscape) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mRotateBtn.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mRotateBtn.setLayoutParams(layoutParams);
        } else {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mRotateBtn.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            mRotateBtn.setLayoutParams(layoutParams);
        }
    }

    //设置播放模式  VideoPlayerTextureView.TRANS_MODE_CENTER_CROP | VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE
    public void setTransMode(int transMode) {
        if (mTransMode != transMode) {
            mTransMode = transMode;
            mVideoView.setVideoTransMode(mTransMode);
        }
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

    //判断是否处于激活状态  调用play之后没有destory
    public boolean isActivate() {
        return mIsActivate;
    }

    //判断是否处于播放状态 与播放按钮状态同步
    public boolean isPlaying() {
        return mIsPlaying;
    }

    //播放进度
    public long getCurrentPosition() {
        if (mVideoPlayerPresenter != null) {
            return mVideoPlayerPresenter.getCurrentPosition();
        }
        return 0l;
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
                //横屏流都显示转屏幕按钮
                mRotateBtn.setVisibility(View.VISIBLE);
                mRotateBtn.setSelected(true);
            }
        } else {
            mRotateBtn.setVisibility(View.GONE);
            mRotateBtn.setSelected(false);
        }
    }


    public void onSeekBarContainerClick() {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        if (isSeekBarContainerVisible()) {
            setSeekBarContainerVisible(false);
        } else {
            setSeekBarContainerVisible(true);
        }
    }

    //网络变化toast提示
    protected void wifiTo4g() {
        if (isActivate()) {
            ToastUtils.showToast(com.live.module.common.R.string.feeds_detail_wifi_2_4g_hint);
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

    private static class MyUIHandler extends Handler {

        private final WeakReference<VideoPlayerPresenterEx> mFeedsVideoPlayer;

        public MyUIHandler(VideoPlayerPresenterEx feedsVideoPlayer) {
            super(Looper.getMainLooper());
            mFeedsVideoPlayer = new WeakReference<>(feedsVideoPlayer);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoPlayerPresenterEx feedsVideoPlayer = mFeedsVideoPlayer.get();
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

}
