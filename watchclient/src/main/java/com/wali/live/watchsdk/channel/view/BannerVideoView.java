package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.base.utils.network.NetworkUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.video.player.IPlayerCallBack;
import com.wali.live.video.player.PlayConfig;
import com.wali.live.video.player.presenter.VideoPlayerPresenter;
import com.wali.live.video.widget.player.VideoPlayerTextureView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.util.HolderUtils;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.eventbus.EventClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.video.player.presenter.VideoPlayerPresenter.MODE_EXOPLAYER;


/**
 * Created by lan on 16/9/8.
 *
 * @module 频道
 */
public class BannerVideoView extends RelativeLayout implements IPlayerCallBack {
    private static final String TAG = BannerVideoView.class.getSimpleName();
    protected VideoPlayerPresenter mVideoPresenter;
    BaseImageView mBannerIv;
    VideoPlayerTextureView mVideoView;
    private ChannelLiveViewModel.BaseLiveItem mLiveItem;
    private long mChannelId;

    boolean isPause = false;//标记LiveMainActivity是否Pause了
    private int mEventHash;
    private boolean mIsSelected;  // 是否选中当前频道
    private ImageView mVolumeBtn;
    private boolean isSoundEnable;
    private boolean isInScreen = false;//是否在可显示窗口范围内
    private Runnable mVideoRunnable = new Runnable() {
        @Override
        public void run() {
            boolean isActivityResume = false;
            if (getContext() instanceof BaseActivity) {
                isActivityResume = ((BaseActivity) getContext()).isActivityForeground();
            }
            MyLog.d(TAG, "mVideoRunnable " + isActivityResume);
            if (isInScreen && !isPause && mIsSelected && NetworkUtils.isWifi(GlobalData.app())
                    && isActivityResume) {
                startVideo();
            } else {
                showCover();
            }
        }
    };

    public BannerVideoView(Context context) {
        super(context);
        init();
    }

    public BannerVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BannerVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void changeSoundEnable(boolean soundEnable) {
        isSoundEnable = soundEnable;
        if (!isSoundEnable) {
            if (null != mVolumeBtn) {
                mVolumeBtn.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.video_volume_off_icon));
            }
            if (mVideoPresenter != null) {
                mVideoPresenter.setVolume(0, 0);
            }
        } else {
            if (null != mVolumeBtn) {
                mVolumeBtn.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.video_volume_on_icon));
            }
            if (mVideoPresenter != null) {
                mVideoPresenter.setVolume(1, 1);
            }
        }
    }

    private void initVolume() {
        AudioManager audioManager = (AudioManager) GlobalData.app().getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0.5f * maxVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (0.5f * maxVolume), 0);
            MyLog.w(TAG, "Volume is too large,init to 0.5");
        }
    }

    public void init() {
        inflate(getContext(), R.layout.video_banner_view, this);
        initVolume();
        mBannerIv = (BaseImageView) this.findViewById(R.id.banner_iv);
        mVideoView = (VideoPlayerTextureView) this.findViewById(R.id.video_player_view);
        mVideoView.getPlayerPresenter().setMode(MODE_EXOPLAYER);
        mVideoView.setVideoTransMode(VideoPlayerTextureView.TRANS_MODE_CENTER_CROP);
        mVolumeBtn = (ImageView) findViewById(R.id.volume_btn);
        mVolumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSoundEnable(!isSoundEnable);
            }
        });
        mVolumeBtn.setVisibility(GONE);
        changeSoundEnable(false);
    }

    public void postInit(long channelId) {
        setChannelId(channelId);
    }

    private void setChannelId(long channelId) {
        mChannelId = channelId;
    }

    public void postVideoRunnable() {

        ThreadPool.getUiHandler().removeCallbacks(mVideoRunnable);
        ThreadPool.getUiHandler().postDelayed(mVideoRunnable, 500);
    }

    public void removeVideoRunnable() {
        MyLog.d(TAG, "removeVideoRunnable");
        ThreadPool.getUiHandler().removeCallbacks(mVideoRunnable);
        stopVideo();
        showCover();
    }


    public void setVideoTransMode(int videoTransMode) {
        mVideoView.setVideoTransMode(videoTransMode);
    }

    private void startVideo() {
        MyLog.w(TAG, "startVideo");
        if (mLiveItem == null || TextUtils.isEmpty(mLiveItem.getVideoUrl())) {
            return;
        }
        if (mVideoPresenter == null) {
            mVideoPresenter = (VideoPlayerPresenter) mVideoView.getPlayerPresenter();
            mVideoPresenter.setVideoPlayerCallBack(this);
            mVideoPresenter.setNeedReset(false);
        }
        if (!mVideoPresenter.isPlaying()) {
            setVideoPath(mLiveItem.getId(), mLiveItem.getVideoUrl());
            showCover();
        }
    }

    private void showCover() {

        MyLog.w(TAG, "showCover");
        mBannerIv.setVisibility(View.VISIBLE);
        HolderUtils.bindImage(mBannerIv, mLiveItem.getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_LARGE), false, 1000, 500, ScalingUtils.ScaleType.CENTER_CROP);
    }

    private void hideCover() {
        MyLog.w(TAG, "hideCover");
        mBannerIv.setVisibility(View.INVISIBLE);
    }

    public void bindData(ChannelLiveViewModel.BaseLiveItem item) {
        MyLog.w(TAG, "bindData" + (item != null ? item.getId() : " null") + " video url: " + item.getVideoUrl());
        mLiveItem = item;
        showCover();
        if (mIsSelected) {
            postVideoRunnable();
        }
    }

    private void setVideoPath(String id, String videoUrl) {
        mVideoPresenter.setVideoPath(id, videoUrl, null, PlayConfig.INTERRUPT_MODE_RELEASE_CREATE);
        mVideoPresenter.setVolume(isSoundEnable ? 1 : 0, isSoundEnable ? 1 : 0);
    }

    public void stopVideo() {
        MyLog.v(TAG, "stopVideo");
        if (mVideoPresenter != null) {
            mVideoPresenter.release();
        }
    }

    public void enterWatch() {
        stopVideo();
    }

    public ImageView getVolumeBtn() {
        return mVolumeBtn;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onPrepared() {
        MyLog.w(TAG, "onPrepared");
        mVolumeBtn.setVisibility(VISIBLE);
        hideCover();
    }

    @Override
    public void onCompletion() {

    }

    @Override
    public void onError(int errCode) {
        MyLog.e(TAG, "onError errCode=" + errCode);
    }

    @Override
    public void onBufferingUpdate(int percent) {
    }

    @Override
    public void onInfo(int info) {
    }

    @Override
    public void onInfo(Message msg) {
    }

    @Override
    public void onSeekComplete() {
    }

    @Override
    public void requestOrientation(int playMode) {
    }

    @Override
    public void onReleased() {
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MyLog.e(TAG, "onAttachedToWindow");
        isInScreen = true;
        EventBus.getDefault().register(this);
        if (mIsSelected) {
            postVideoRunnable();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MyLog.e(TAG, "onDetachedFromWindow");
        isInScreen = false;
        unregisterEventBus();
        removeVideoRunnable();
    }

    private void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    protected void bindTextView(TextView tv, String text) {
        if (tv == null) {
            return;
        }
        if (!TextUtils.isEmpty(text)) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(EventClass.SelectChannelEvent event) {
        if (event == null) {
            return;
        }
        MyLog.w(TAG, " onEventMainThread SelectChannelEvent  event channelId=" + event.channelId + " ownId=" + mChannelId + " hashcode=" + event.hashCode());
        // 说明是同一事件
        if (mEventHash == event.hashCode() && mIsSelected) {
            return;
        }
        mEventHash = event.hashCode();
        mIsSelected = event.channelId == mChannelId;
        if (mIsSelected && !isPause) {
            postVideoRunnable();
        } else {
            removeVideoRunnable();
        }
//        EventBus.getDefault().removeStickyEvent(event);
    }

    /**
     * 这个主要处理从LiveMianActivity打开一个Fragment时候的情况
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.ChannelVideoCtrlEvent event) {
        MyLog.w(TAG, "onEventMainThread ChannelVideoCtrlEvent event");

        if (event.canPlay) {
            isPause = false;
            if (getLocalVisibleRect(new Rect())) {
                postVideoRunnable();
            }
        } else {
            isPause = true;
            stopVideo();
        }
    }


    /**
     * Activity onResume,onPause
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.LiveListActivityLiveCycle event) {
        if (event == null) {
            return;
        }
        MyLog.d(TAG, " onEventMainThread LiveMainActivityLiveCycle  event =" + event.liveEvent);
        isPause = event.liveEvent == EventClass.LiveListActivityLiveCycle.Event.PAUSE;
        if (event.liveEvent == EventClass.LiveListActivityLiveCycle.Event.RESUME && mIsSelected) {
            postVideoRunnable();
        } else if (event.liveEvent == EventClass.LiveListActivityLiveCycle.Event.PAUSE) {
            removeVideoRunnable();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.StopPlayVideoEvent event) {
        removeVideoRunnable();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MiLinkEvent.StatusConnected event) {
        if (NetworkUtils.isWifi(getContext())) {
            postVideoRunnable();
        }
    }
}
