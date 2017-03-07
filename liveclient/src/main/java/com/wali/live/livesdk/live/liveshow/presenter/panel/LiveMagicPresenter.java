package com.wali.live.livesdk.live.liveshow.presenter.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveMagicPanel;

/**
 * Created by yangli on 2017/03/07.
 * <p>
 * Generated using create_bottom_panel.py
 *
 * @module 秀场美妆面板表现
 */
public class LiveMagicPresenter extends ComponentPresenter<LiveMagicPanel.IView>
        implements LiveMagicPanel.IPresenter {
    private static final String TAG = "LiveMagicPresenter";

    public LiveMagicPresenter(
            @NonNull IComponentController componentController) {
        super(componentController);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                default:
                    break;
            }
            return false;
        }
    }
}
