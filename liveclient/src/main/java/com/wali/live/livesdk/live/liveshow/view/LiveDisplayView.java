package com.wali.live.livesdk.live.liveshow.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.livesdk.R;

/**
 * Created by yangli on 2017/03/08.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 直播大小窗视图
 */
public class LiveDisplayView extends RelativeLayout implements IComponentView<LiveDisplayView.IPresenter, LiveDisplayView.IView> {
    private static final String TAG = "LiveDisplayView";

    @Nullable
    protected IPresenter mPresenter;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(@IdRes int resId, View.OnClickListener listener) {
        View view = $(resId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public LiveDisplayView(Context context) {
        this(context, null);
    }

    public LiveDisplayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveDisplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.live_display_view, this);
    }

    public void onOrientation(boolean isLandscape) {
        MyLog.d(TAG, "onOrientation isLandscape=" + isLandscape);


    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Nullable
            @Override
            public <T extends View> T getRealView() {
                return null;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {

    }
}
