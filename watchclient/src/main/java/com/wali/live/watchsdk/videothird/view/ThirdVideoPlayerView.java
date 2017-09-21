package com.wali.live.watchsdk.videothird.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;

/**
 * Created by yangli on 2017/08/31.
 * <p>
 * Generated using create_view_with_presenter.py
 *
 * @module 第三方播放器视图
 */
public class ThirdVideoPlayerView extends RelativeLayout implements
        IComponentView<ThirdVideoPlayerView.IPresenter, ThirdVideoPlayerView.IView> {
    private static final String TAG = "ThirdVideoPlayerView";

    @NonNull
    protected IPresenter mPresenter;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@NonNull IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public ThirdVideoPlayerView(Context context) {
        this(context, null);
    }

    public ThirdVideoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThirdVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) ThirdVideoPlayerView.this;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {
    }
}
