package com.wali.live.livesdk.live.liveshow.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.component.presenter.BaseContainerPresenter;
import com.wali.live.livesdk.live.liveshow.LiveComponentController;
import com.wali.live.livesdk.live.liveshow.presenter.panel.LiveMagicPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.panel.LivePlusPresenter;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveMagicPanel;
import com.wali.live.livesdk.live.liveshow.view.panel.LivePlusPanel;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveSettingPanel;

/**
 * Created by yangli on 2017/3/13.
 *
 * @module 秀场直播底部面板表现
 */
public class PanelContainerPresenter extends BaseContainerPresenter<RelativeLayout> {
    private static final String TAG = "PanelContainerPresenter";

    private StreamerPresenter mStreamerPresenter;

    private LiveSettingPanel mSettingPanel;
    private LiveMagicPanel mMagicPanel;
    private LivePlusPanel mPlusPanel;

    private LivePlusPresenter mPlusPresenter;
    private LiveMagicPresenter mMagicPresenter;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public PanelContainerPresenter(
            @NonNull ComponentPresenter.IComponentController componentController,
            @NonNull StreamerPresenter streamerPresenter) {
        super(componentController);
        mStreamerPresenter = streamerPresenter;
        registerAction(LiveComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(LiveComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(LiveComponentController.MSG_ON_BACK_PRESSED);
        registerAction(LiveComponentController.MSG_SHOW_SETTING_PANEL);
        registerAction(LiveComponentController.MSG_SHOW_PLUS_PANEL);
        registerAction(LiveComponentController.MSG_SHOW_MAGIC_PANEL);
        registerAction(LiveComponentController.MSG_HIDE_BOTTOM_PANEL);
    }

    @Override
    public void setComponentView(@Nullable RelativeLayout relativeLayout) {
        super.setComponentView(relativeLayout);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanel(true);
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mPlusPanel != null) {
            mPlusPresenter.destroy();
            mPlusPresenter = null;
            mPlusPanel = null;
        }
        if (mMagicPresenter != null) {
            mMagicPresenter.destroy();
            mMagicPresenter = null;
            mMagicPanel = null;
        }
    }

    private void showSettingPanel() {
        if (mSettingPanel == null) {
            mSettingPanel = new LiveSettingPanel(mView, mStreamerPresenter);
        }
        showPanel(mSettingPanel, true);
    }

    private void showPlusPanel() {
        if (mPlusPanel == null) {
            mPlusPanel = new LivePlusPanel(mView);
            mPlusPresenter = new LivePlusPresenter(mComponentController);
            mPlusPanel.setPresenter(mPlusPresenter);
            mPlusPresenter.setComponentView(mPlusPanel.getViewProxy());
        }
        showPanel(mPlusPanel, true);
    }

    private void showMagicPanel() {
        if (mMagicPanel == null) {
            mMagicPanel = new LiveMagicPanel(mView);
            mMagicPresenter = new LiveMagicPresenter(mComponentController);
            mMagicPanel.setPresenter(mMagicPresenter);
            mMagicPresenter.setComponentView(mMagicPanel.getViewProxy());
        }
        showPanel(mMagicPanel, true);
    }

    @Nullable
    @Override
    protected ComponentPresenter.IAction createAction() {
        return new Action();
    }

    public class Action implements ComponentPresenter.IAction {
        @Override
        public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case LiveComponentController.MSG_ON_ORIENT_PORTRAIT:
                    onOrientation(false);
                    return true;
                case LiveComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    onOrientation(true);
                    return true;
                case LiveComponentController.MSG_SHOW_SETTING_PANEL:
                    showSettingPanel();
                    return true;
                case LiveComponentController.MSG_SHOW_PLUS_PANEL:
                    showPlusPanel();
                    return true;
                case LiveComponentController.MSG_SHOW_MAGIC_PANEL:
                    showMagicPanel();
                    return true;
                case LiveComponentController.MSG_HIDE_BOTTOM_PANEL:
                    hidePanel(false);
                    return true;
                case LiveComponentController.MSG_ON_BACK_PRESSED:
                    return hidePanel(true);
                default:
                    break;
            }
            return false;
        }
    }
}
