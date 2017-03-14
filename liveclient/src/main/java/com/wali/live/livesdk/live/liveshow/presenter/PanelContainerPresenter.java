package com.wali.live.livesdk.live.liveshow.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.presenter.Presenter;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.component.presenter.BaseContainerPresenter;
import com.wali.live.livesdk.live.liveshow.LiveComponentController;
import com.wali.live.livesdk.live.liveshow.presenter.panel.LiveMagicPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.panel.LivePlusPresenter;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveMagicPanel;
import com.wali.live.livesdk.live.liveshow.view.panel.LivePlusPanel;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveSettingPanel;

import java.lang.ref.WeakReference;

/**
 * Created by yangli on 2017/3/13.
 *
 * @module 秀场直播底部面板表现
 */
public class PanelContainerPresenter extends BaseContainerPresenter<RelativeLayout> {
    private static final String TAG = "PanelContainerPresenter";

    private StreamerPresenter mStreamerPresenter;

    private WeakReference<LiveSettingPanel> mSettingPanelRef;
    private WeakReference<LiveMagicPanel> mMagicPanelRef;
    private WeakReference<LivePlusPanel> mPlusPanelRef;

    private WeakReference<LivePlusPresenter> mPlusPresenterRef;
    private WeakReference<LiveMagicPresenter> mMagicPresenterRef;

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

        mSettingPanelRef = null;

        Presenter plusPresenter = deRef(mPlusPresenterRef);
        if (plusPresenter != null) {
            plusPresenter.destroy();
        }
        mPlusPresenterRef = null;
        mPlusPanelRef = null;

        Presenter magicPresenter = deRef(mMagicPresenterRef);
        if (magicPresenter != null) {
            magicPresenter.destroy();
        }
        mMagicPresenterRef = null;
        mMagicPanelRef = null;
    }

    private void showSettingPanel() {
        LiveSettingPanel panel = deRef(mSettingPanelRef);
        if (panel == null) {
            panel = new LiveSettingPanel(mView, mStreamerPresenter);
            mSettingPanelRef = new WeakReference<>(panel);
        }
        showPanel(panel, true);
    }

    private void showPlusPanel() {
        LivePlusPanel panel = deRef(mPlusPanelRef);
        if (panel == null) {
            panel = new LivePlusPanel(mView);
            mPlusPanelRef = new WeakReference<>(panel);
            LivePlusPresenter presenter = deRef(mPlusPresenterRef);
            if (presenter == null) {
                presenter = new LivePlusPresenter(mComponentController);
                mPlusPresenterRef = new WeakReference<>(presenter);
            }
            panel.setPresenter(presenter);
            presenter.setComponentView(panel.getViewProxy());
        }
        showPanel(panel, true);
    }

    private void showMagicPanel() {
        LiveMagicPanel panel = deRef(mMagicPanelRef);
        if (panel == null) {
            panel = new LiveMagicPanel(mView, mStreamerPresenter);
            mMagicPanelRef = new WeakReference<>(panel);
            LiveMagicPresenter presenter = deRef(mMagicPresenterRef);
            if (presenter == null) {
                presenter = new LiveMagicPresenter();
                mMagicPresenterRef = new WeakReference<>(presenter);
            }
            panel.setPresenter(presenter);
            presenter.setComponentView(panel.getViewProxy());
        }
        showPanel(panel, true);
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
                    hidePanel(true);
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
