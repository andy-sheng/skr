package com.wali.live.watchsdk.videothird.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.video.widget.player.ReplaySeekBar;
import com.wali.live.watchsdk.R;

/**
 * Created by yangli on 2017/09/22.
 *
 * @module 播放控制视图
 */
public class VideoControlView extends RelativeLayout
        implements IComponentView<VideoControlView.IPresenter, VideoControlView.IView> {
    private static final String TAG = "VideoControlView";

    @Nullable
    protected IPresenter mPresenter;

    private ReplaySeekBar mSeekBar;
    private ImageButton mPlayBtn;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public VideoControlView(Context context) {
        this(context, null);
    }

    public VideoControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.video_control_view, this);
        mSeekBar = $(R.id.video_seek_bar);
        mPlayBtn = $(R.id.play_button);
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) VideoControlView.this;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {
    }
}
