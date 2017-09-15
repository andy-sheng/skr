package com.wali.live.watchsdk.component.presenter.panel;

import android.support.annotation.NonNull;
import android.util.Log;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.panel.LinkInfoPanel;

import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

/**
 * Created by yangli on 2017/09/14.
 * <p>
 * Generated using create_panel_with_presenter.py
 *
 * @module 主播-主播连麦信息面板表现
 */
public class LinkInfoPresenter extends ComponentPresenter<LinkInfoPanel.IView>
        implements LinkInfoPanel.IPresenter {
    private static final String TAG = "LinkInfoPresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public boolean isShow() {
        return mView != null && mView.isShow();
    }

    public LinkInfoPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
    }

    @Override
    public void stopPresenter() {
        MyLog.d(TAG, "stopPresenter");
        super.stopPresenter();
        unregisterAllAction();
    }

    public void onLinkStart(long userId, String userName, boolean isLandscape) {
        MyLog.w("onLinkStart");
        mView.showSelf(true, isLandscape);
        mView.updateLinkUserInfo(userId, userName);
    }

    public void onLinkStop() {
        MyLog.w("onLinkStop");
        mView.hideSelf(true);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            Log.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mView.onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mView.onOrientation(true);
                return true;
            default:
                break;
        }
        return false;
    }
}
