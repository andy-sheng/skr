package com.wali.live.livesdk.live.liveshow.presenter;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.liveshow.LiveComponentController;
import com.wali.live.livesdk.live.liveshow.view.panel.FloatAtmospherePanel;

/**
 * Created by yangli on 2017/03/13.
 *
 * @module 浮层容器表现
 */
public class FloatContainerPresenter extends ComponentPresenter<RelativeLayout> {
    private static final String TAG = "FloatContainerPresenter";

    private StreamerPresenter mStreamerPresenter;

    private FloatAtmospherePanel mAtmospherePanel;

    protected boolean mIsLandscape = false;

    public FloatContainerPresenter(
            @NonNull IComponentController componentController,
            @NonNull StreamerPresenter streamerPresenter) {
        super(componentController);
        mStreamerPresenter = streamerPresenter;
        registerAction(LiveComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(LiveComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(LiveComponentController.MSG_SHOW_ATMOSPHERE_VIEW);
    }

    public void onOrientation(boolean isLandscape) {
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        if (mAtmospherePanel != null && mAtmospherePanel.isShow()) {
            mAtmospherePanel.onOrientation(isLandscape);
        }
    }

    private void showAtmospherePanel() {
        if (mAtmospherePanel == null) {
            mAtmospherePanel = new FloatAtmospherePanel(mView, mStreamerPresenter);
        }
        mAtmospherePanel.showSelf(true, mIsLandscape);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            switch (source) {
                case LiveComponentController.MSG_ON_ORIENT_PORTRAIT:
                    onOrientation(false);
                    return true;
                case LiveComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    onOrientation(true);
                    return true;
                case LiveComponentController.MSG_SHOW_ATMOSPHERE_VIEW:
                    showAtmospherePanel();
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}
