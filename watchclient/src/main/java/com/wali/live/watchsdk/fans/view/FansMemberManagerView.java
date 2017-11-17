package com.wali.live.watchsdk.fans.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;

/**
 * Created by yangli on 2017/11/16.
 *
 * @module 粉丝团成员管理页视图
 */
public class FansMemberManagerView extends RelativeLayout
        implements IComponentView<FansMemberManagerView.IPresenter, FansMemberManagerView.IView> {
    private static final String TAG = "FansMemberManagerView";

    @Nullable
    protected IPresenter mPresenter;

    public FansMemberManagerView(Context context) {
        this(context, null);
    }

    public FansMemberManagerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FansMemberManagerView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return null; // [TODO-COMPONENT return real view of proxy]
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {
    }
}
