package com.wali.live.livesdk.live.liveshow.presenter;


import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.componentwrapper.BaseSdkController;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.liveshow.view.panel.FloatAtmospherePanel;

import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_ATMOSPHERE_VIEW;

/**
 * Created by yangli on 2017/03/13.
 *
 * @module 浮层容器表现
 */
public class FloatContainerPresenter extends ComponentPresenter<RelativeLayout, BaseSdkController> {
    private static final String TAG = "FloatContainerPresenter";

    private StreamerPresenter mStreamerPresenter;

    private FloatAtmospherePanel mAtmospherePanel;

    protected boolean mIsLandscape = false;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public FloatContainerPresenter(
            @NonNull BaseSdkController controller,
            @NonNull StreamerPresenter streamerPresenter) {
        super(controller);
        mStreamerPresenter = streamerPresenter;
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_SHOW_ATMOSPHERE_VIEW);
    }

    public void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
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

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                onOrientation(true);
                return true;
            case MSG_SHOW_ATMOSPHERE_VIEW:
                showAtmospherePanel();
                return true;
            default:
                break;
        }
        return false;
    }
}
