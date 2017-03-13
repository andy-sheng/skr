package com.wali.live.livesdk.live.liveshow.presenter.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveSettingPanel;

/**
 * Created by yangli on 2017/03/07.
 * <p>
 * Generated using create_bottom_panel.py
 *
 * @module 秀场设置面板表现
 */
public class LiveSettingPresenter extends ComponentPresenter<LiveSettingPanel.IView>
        implements LiveSettingPanel.IPresenter {
    private static final String TAG = "LiveSettingPresenter";

    public LiveSettingPresenter(@NonNull IComponentController componentController) {
        super(componentController);
    }

    @Override
    public void setVolume(int volume) {
    }

    @Override
    public void setReverb(int reverb) {
    }

    @Override
    public void enableHifi(boolean enable) {
    }

    @Override
    public void switchCamera() {
    }

    @Override
    public boolean isBackCamera() {
        return false;
    }

    @Override
    public void enableMirrorImage(boolean enable) {
    }

    @Override
    public void enableFlashLight(boolean enable) {
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
