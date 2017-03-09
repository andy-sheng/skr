package com.wali.live.livesdk.live.liveshow.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.wali.live.component.view.BasePanelContainer;
import com.wali.live.component.view.IOrientationListener;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;

/**
 * Created by yangli on 17-2-14.
 *
 * @module 底部面板视图, 游戏直播
 */
public class LivePanelContainer extends BasePanelContainer<LivePanelContainer.IPresenter,
        LivePanelContainer.IView, RelativeLayout> {

    public LivePanelContainer(@NonNull RelativeLayout panelContainer) {
        super(panelContainer);
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public boolean processBackPress() {
                return LivePanelContainer.this.hidePanel(true);
            }

            @Override
            public boolean showPanel(@Nullable BaseBottomPanel panel) {
                LivePanelContainer.this.showPanel(panel, true);
                return true;
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                LivePanelContainer.this.onOrientation(isLandscape);
            }

            @Nullable
            @Override
            public <T extends View> T getRealView() {
                return (T) mPanelContainer;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy, IOrientationListener {
        /**
         * 响应返回键事件
         */
        boolean processBackPress();

        /**
         * 显示面板
         */
        boolean showPanel(@Nullable BaseBottomPanel panel);
    }

}
