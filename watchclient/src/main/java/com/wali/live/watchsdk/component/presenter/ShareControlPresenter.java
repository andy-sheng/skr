package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.panel.ShareControlPanel;

/**
 * Created by chenyong on 2017/4/27.
 */

public class ShareControlPresenter extends ComponentPresenter<ShareControlPanel.IView>
        implements ShareControlPanel.IPresenter {
    private static final String TAG = "ShareControlPresenter";

    public ShareControlPresenter(@NonNull IComponentController componentController) {
        super(componentController);
        registerAction(ComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(ComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(ComponentController.MSG_SHOW_SHARE_PANEL);
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
                case ComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mView.onOrientation(false);
                    return true;
                case ComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mView.onOrientation(true);
                    return true;
                case ComponentController.MSG_SHOW_SHARE_PANEL:
                    mView.showSelf();
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}
