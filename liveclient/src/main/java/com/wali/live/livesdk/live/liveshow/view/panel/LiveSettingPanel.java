package com.wali.live.livesdk.live.liveshow.view.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;

/**
 * Created by yangli on 2017/03/07.
 * <p>
 * Generated using create_bottom_panel.py
 *
 * @module 秀场设置面板视图
 */
public class LiveSettingPanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements View.OnClickListener, IComponentView<LiveSettingPanel.IPresenter, LiveSettingPanel.IView> {
    private static final String TAG = "LiveSettingPanel";

    @Nullable
    protected IPresenter mPresenter;

    // Auto-generated to easy use setOnClickListener
    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected int getLayoutResId() {
        return 0;
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public LiveSettingPanel(@NonNull RelativeLayout parentView) {
        super(parentView);
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
