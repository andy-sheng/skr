package com.wali.live.watchsdk.videodetail.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.view.RotatedSeekBar;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;

/**
 * Created by yangli on 2017/09/25.
 *
 * @module 详情-播放控制视图
 */
public class DetailPlayerView extends RelativeLayout implements View.OnClickListener,
        IComponentView<DetailPlayerView.IPresenter, DetailPlayerView.IView> {
    private static final String TAG = "DetailPlayerView";

    @Nullable
    protected IPresenter mPresenter;

    private int mProgress;
    private int mDuration;
    private boolean mSeekTouching = false;
    private boolean mIsShow = true;

    private TextureView mTextureView;

    private View mLoadingView;
    private View mVideoCtrlArea;
    private ImageView mPlayBtn;
    private TextView mCurrTimeView;
    private TextView mTotalTimeView;
    private View mFullScreenBtn;
    private RotatedSeekBar mSeekBar;

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsShow && mPlayBtn.isSelected()) {
                mIsShow = false;
                mVideoCtrlArea.setVisibility(View.GONE);
            }
        }
    };

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
        if (mPresenter == null) {
            return;
        }
        int i = v.getId();
        if (i == R.id.video_texture_view) {
            changeViewVisibility();
            return;
        } else if (i == R.id.play_btn) {
            onPlayBtnClick(!v.isSelected());
        } else if (i == R.id.full_screen_btn) {
            mPresenter.switchToFullScreen();
        }
        removeCallbacks(mHideRunnable);
        postDelayed(mHideRunnable, 5000);
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
        mTextureView.setSurfaceTextureListener(mPresenter);
    }

    public DetailPlayerView(Context context) {
        this(context, null);
    }

    public DetailPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DetailPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.detail_player_view, this);

        mTextureView = $(R.id.video_texture_view);

        mLoadingView = $(R.id.loading_view);
        mVideoCtrlArea = $(R.id.video_ctrl_area);
        mPlayBtn = $(R.id.play_btn);
        mCurrTimeView = $(R.id.curr_time_view);
        mTotalTimeView = $(R.id.total_time_view);
        mFullScreenBtn = $(R.id.full_screen_btn);
        mSeekBar = $(R.id.seek_bar);

        $click(mPlayBtn, this);
        $click(mFullScreenBtn, this);
        $click(mSeekBar, this);
        $click(mVideoCtrlArea, this);
        $click(mTextureView, this);

        mSeekBar.setOnRotatedSeekBarChangeListener(new RotatedSeekBar.OnRotatedSeekBarChangeListener() {
            @Override
            public void onProgressChanged(RotatedSeekBar rotatedSeekBar, float percent, boolean fromUser) {
                mProgress = (int) (mDuration * percent);
                mCurrTimeView.setText(String.format("%02d:%02d", mProgress / 60, mProgress % 60));
            }

            @Override
            public void onStartTrackingTouch(RotatedSeekBar rotatedSeekBar) {
                mSeekTouching = true;
                removeCallbacks(mHideRunnable);
            }

            @Override
            public void onStopTrackingTouch(RotatedSeekBar rotatedSeekBar) {
                mPresenter.seekTo(mDuration * rotatedSeekBar.getPercent());
                mSeekTouching = false;
                postDelayed(mHideRunnable, 5000);
            }
        });
    }

    private void changeViewVisibility() {
        mIsShow = !mIsShow;
        if (mIsShow) {
            mVideoCtrlArea.setVisibility(View.VISIBLE);
            removeCallbacks(mHideRunnable);
            postDelayed(mHideRunnable, 5000);
        } else {
            mVideoCtrlArea.setVisibility(View.GONE);
        }
    }

    private void onPlayBtnClick(boolean isResume) {
        if (isResume) {
            mPresenter.resumePlay();
        } else {
            mPresenter.pausePlay();
        }
    }

    public void switchToReplayMode() {
        mFullScreenBtn.setVisibility(View.GONE);
        mTextureView.setClickable(false);
    }

    public void switchToDetailMode() {
        mFullScreenBtn.setVisibility(View.VISIBLE);
        mTextureView.setClickable(true);
    }

    public void switchToThirdMode() {
        mFullScreenBtn.setVisibility(View.GONE);
        mTextureView.setClickable(true);
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) DetailPlayerView.this;
            }

            @Override
            public void onPlayResumed() {
                postDelayed(mHideRunnable, 5000);
                mPlayBtn.setSelected(true);
            }

            @Override
            public void onPlayPaused() {
                mPlayBtn.setSelected(false);
                if (!mIsShow) {
                    mIsShow = true;
                    mVideoCtrlArea.setVisibility(View.VISIBLE);
                } else {
                    removeCallbacks(mHideRunnable);
                }
                showLoading(false);
            }

            @Override
            public void reset() {
                mSeekBar.setPercent(0);
                onPlayPaused();
            }

            @Override
            public final void showLoading(boolean isShow) {
                mLoadingView.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onUpdateDuration(int duration) {
                mDuration = duration;
                mTotalTimeView.setText(String.format("%02d:%02d", duration / 60, duration % 60));
                mSeekBar.setEnabled(mDuration > 0);
            }

            @Override
            public void onUpdateProgress(int progress) {
                if (mSeekTouching) {
                    return;
                }
                mProgress = progress;
                mCurrTimeView.setText(String.format("%02d:%02d", progress / 60, progress % 60));
                if (mDuration != 0) {
                    mSeekBar.setPercent((float) mProgress / mDuration);
                }
            }

            @Override
            public void onChangeVisibility() {
                changeViewVisibility();
            }
        }
        return new ComponentView();
    }

    public interface IPresenter extends TextureView.SurfaceTextureListener {
        /**
         * 恢复播放
         */
        void resumePlay();

        /**
         * 暂停播放
         */
        void pausePlay();

        /**
         * 快进
         */
        void seekTo(float progress);

        /**
         * 切换到全屏
         */
        void switchToFullScreen();
    }

    public interface IView extends IViewProxy {
        /**
         * 播放开始
         */
        void onPlayResumed();

        /**
         * 播放暂停
         */
        void onPlayPaused();

        /**
         * 重置View的状态
         */
        void reset();

        /**
         * 显示加载等待图标
         */
        void showLoading(boolean isShow);

        /**
         * 更新视频时长
         */
        void onUpdateDuration(int duration);

        /**
         * 更新当前播放进度
         */
        void onUpdateProgress(int progress);

        /**
         * 更新控制界面可见行
         */
        void onChangeVisibility();
    }
}
