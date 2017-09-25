package com.wali.live.watchsdk.videodetail.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.view.RotatedSeekBar;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.video.widget.player.HotSpotSeekBar;
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

    private SurfaceView mSurfaceView;

    private ImageView mPlayBtn;
    private TextView mCurrTimeView;
    private TextView mTotalTimeView;
    private View mFullScreenBtn;
    private HotSpotSeekBar mSeekBar;

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
        if (i == R.id.play_btn) {
            boolean selected = !v.isSelected();
            v.setSelected(selected);
            if (selected) {
                mPresenter.pausePlay();
            } else {
                mPresenter.startPlay();
            }
        } else if (i == R.id.full_screen_btn) {
            mPresenter.switchToReplayMode();
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
        mSurfaceView.getHolder().addCallback(mPresenter);
    }

    public SurfaceHolder getVideoDisplay() {
        return mSurfaceView != null ? mSurfaceView.getHolder() : null;
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

        mSurfaceView = $(R.id.video_surface_view);

        mPlayBtn = $(R.id.play_btn);
        mCurrTimeView = $(R.id.curr_time_view);
        mTotalTimeView = $(R.id.total_time_view);
        mFullScreenBtn = $(R.id.full_screen_btn);
        mSeekBar = $(R.id.seek_bar);

        $click(mPlayBtn, this);
        $click(mFullScreenBtn, this);

        mSeekBar.setOnRotatedSeekBarChangeListener(new RotatedSeekBar.OnRotatedSeekBarChangeListener() {
            @Override
            public void onProgressChanged(RotatedSeekBar rotatedSeekBar, float percent, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(RotatedSeekBar rotatedSeekBar) {
            }

            @Override
            public void onStopTrackingTouch(RotatedSeekBar rotatedSeekBar) {
            }
        });
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) DetailPlayerView.this;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter extends SurfaceHolder.Callback {
        /**
         * 恢复播放
         */
        void startPlay();

        /**
         * 暂停播放
         */
        void pausePlay();

        /**
         * 切换到全屏
         */
        void switchToReplayMode();

        /**
         * 切换到半屏
         */
        void switchToDetailMode();
    }

    public interface IView extends IViewProxy {
    }
}
