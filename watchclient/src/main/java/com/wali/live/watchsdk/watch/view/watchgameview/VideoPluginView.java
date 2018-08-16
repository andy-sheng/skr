package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.base.utils.date.DateTimeUtils;
import com.thornbirds.component.IEventController;
import com.wali.live.video.view.VideoSeekBar;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.GameIntroVideoPresenter;

/**
 * 一个完整的播放器 view 组件，有seekbar等
 */
public class VideoPluginView extends RelativeLayout {

    TextureView mVideoView;
    RelativeLayout mPlayerControlContainer;
    ImageView mVoiceBtn;
    TextView mPlayerTimeTv;
    VideoSeekBar mVideoSeekbar;
    ImageView mPlayerControlBtn;

    IEventController mIEventController;

    private GameIntroVideoPresenter mGameIntroVideoPresenter;

    Handler mUIHandler = new Handler();

    String mVideoUrl;

    public VideoPluginView(Context context) {
        super(context);
        init(context);
    }

    public VideoPluginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoPluginView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 更新播放进度条
     */
    Runnable mOnSeekProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mGameIntroVideoPresenter != null) {
                if (mGameIntroVideoPresenter.isStarted() && !mGameIntroVideoPresenter.isPause()) {
                    long mPlayedTime = mGameIntroVideoPresenter.getCurrentPosition();
                    long mTotalTime = mGameIntroVideoPresenter.getDuration();
                    if (mVideoSeekbar != null) {
                        mVideoSeekbar.setProgress(mPlayedTime, mTotalTime);
                        mUIHandler.postDelayed(mOnSeekProgressRunnable, 500);
                    }
                }
            }
        }
    };


    GameIntroVideoPresenter getGameIntroVideoPresenter() {
        if (mGameIntroVideoPresenter == null) {
            mGameIntroVideoPresenter = new GameIntroVideoPresenter(mIEventController);
            mGameIntroVideoPresenter.setView(mVideoView);
            mGameIntroVideoPresenter.startPresenter();
        }
        return mGameIntroVideoPresenter;
    }

    public void setEventController(IEventController eventController) {
        mIEventController = eventController;
    }

    private void init(Context context) {
        inflate(context, R.layout.video_plugin_view_layout, this);

        mVideoView = (TextureView) this.findViewById(R.id.video_view);
        mPlayerControlContainer = (RelativeLayout) this.findViewById(R.id.player_control_container);
        mVoiceBtn = (ImageView) this.findViewById(R.id.voice_btn);
        mPlayerTimeTv = (TextView) this.findViewById(R.id.player_time_tv);
        mVideoSeekbar = (VideoSeekBar) this.findViewById(R.id.video_seekbar);
        mPlayerControlBtn = (ImageView) this.findViewById(R.id.player_control_btn);

        mVideoSeekbar.setVideoSeekBarListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long mPlayedTime = mGameIntroVideoPresenter.getCurrentPosition();
                long mTotalTime = mGameIntroVideoPresenter.getDuration();
                String t = DateTimeUtils.formatLocalVideoTime(mPlayedTime) + "/" + DateTimeUtils.formatLocalVideoTime(mTotalTime);
                mPlayerTimeTv.setText(t);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                long p = seekBar.getProgress() * getGameIntroVideoPresenter().getDuration() / 100;
                getGameIntroVideoPresenter().seekTo(p);
            }
        });

        mVoiceBtn = (ImageView) this.findViewById(R.id.voice_btn);
        mPlayerTimeTv = (TextView) this.findViewById(R.id.player_time_tv);
        mVoiceBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceBtn.isSelected()) {
                    // 声音开启
                    mVoiceBtn.setSelected(false);
                    getGameIntroVideoPresenter().mute(false);
                } else {
                    // 声音关闭
                    mVoiceBtn.setSelected(true);
                    getGameIntroVideoPresenter().mute(true);
                }
            }
        });

        mPlayerControlBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerControlBtn.isSelected()) {
                    mPlayerControlBtn.setSelected(false);
                    getGameIntroVideoPresenter().pauseVideo();
                } else {
                    // 视频开始播放
                    mPlayerControlBtn.setVisibility(GONE);
                    mPlayerControlBtn.setSelected(true);
                    mVideoView.setVisibility(VISIBLE);
                    getGameIntroVideoPresenter().setOriginalStreamUrl(mVideoUrl);
                    mUIHandler.post(mOnSeekProgressRunnable);

//                    int position = mGamePreviewViewPager.getCurrentItem();
//                    Object object = mGamePreviewPagerAdapter.getItemByPosition(position);
//                    if (object instanceof GameInfoModel.GameVideo) {
//                        GameInfoModel.GameVideo gameVideo = (GameInfoModel.GameVideo) object;
//                        List<GameInfoModel.GameVideo.VideoBaseInfo> list = gameVideo.getVideoInfoList();
//                        if (list.size() > 0) {
//                            GameInfoModel.GameVideo.VideoBaseInfo baseInfo = list.get(0);
//                            mVideoView.setVisibility(VISIBLE);
//
//                            if (getGameIntroVideoPresenter().isStarted()) {
//                                getGameIntroVideoPresenter().resumeVideo();
//                            } else {
//                                getGameIntroVideoPresenter().startVideo();
//                            }
//
//                        }
//                    }
                }
            }
        });

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                processControlView();
            }
        });
    }

    void processControlView() {
        if (mPlayerControlBtn.isSelected()) {
            //按钮是暂停状态，说明在播放中
            if (mPlayerControlContainer.getVisibility() == VISIBLE) {
                //控制面板可见，再点击应该立刻消失
                mPlayerControlContainer.setVisibility(GONE);
                mPlayerControlBtn.setVisibility(GONE);
            } else {
                // 如果控制面板不可见，点击应该出现，3s后无操作就消失
                mPlayerControlBtn.setVisibility(VISIBLE);
                mPlayerControlBtn.setVisibility(VISIBLE);
            }
        } else {
            //按钮是播放状态，说明目前是暂停的
            if (mPlayerControlContainer.getVisibility() == VISIBLE) {
                //控制面板可见，再点击应该立刻消失
                mPlayerControlContainer.setVisibility(GONE);
                mPlayerControlBtn.setVisibility(GONE);
            } else {
                // 如果控制面板不可见，点击应该出现，3s后无操作就消失
                mPlayerControlBtn.setVisibility(VISIBLE);
                mPlayerControlBtn.setVisibility(VISIBLE);
            }
        }
    }

    public void setVideoUrl(String url) {
        if (url.equals(mVideoUrl)) {

        } else {
            if (mGameIntroVideoPresenter != null) {
                mGameIntroVideoPresenter.stopVideo();
                mVideoView.setVisibility(GONE);

                mPlayerControlBtn.setSelected(false);
                mPlayerControlBtn.setVisibility(VISIBLE);

            }
        }
        mVideoUrl = url;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mGameIntroVideoPresenter != null) {
            mGameIntroVideoPresenter.startPresenter();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mGameIntroVideoPresenter != null) {
            mGameIntroVideoPresenter.stopPresenter();
        }
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }
    }

    public View getVideoView() {
        return mVideoView;
    }

    public void tryPauseVideo() {
        if (mGameIntroVideoPresenter != null && mGameIntroVideoPresenter.isStarted()) {
            mGameIntroVideoPresenter.pauseVideo();

            mPlayerControlBtn.setSelected(false);
            mPlayerControlBtn.setVisibility(VISIBLE);
        }
    }
}
