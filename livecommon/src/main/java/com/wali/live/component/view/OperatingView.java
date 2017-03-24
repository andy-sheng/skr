package com.wali.live.component.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by chenyong on 2017/03/24.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 运营位组件
 */
public class OperatingView extends RelativeLayout
        implements IComponentView<OperatingView.IPresenter, OperatingView.IView> {
    private static final String TAG = "OperatingView";

    @Nullable
    protected IPresenter mPresenter;

    public OperatingView(Context context) {
        super(context);
    }

    public OperatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OperatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

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

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Nullable
            @Override
            public <T extends View> T getRealView() {
                return (T) OperatingView.this;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {
    }
}
