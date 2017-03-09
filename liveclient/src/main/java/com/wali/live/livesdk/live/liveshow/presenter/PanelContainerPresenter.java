package com.wali.live.livesdk.live.liveshow.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.livesdk.live.liveshow.LiveComponentController;
import com.wali.live.livesdk.live.liveshow.presenter.panel.LivePlusPresenter;
import com.wali.live.livesdk.live.liveshow.view.LivePanelContainer;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveMagicPanel;
import com.wali.live.livesdk.live.liveshow.view.panel.LivePlusPanel;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveSettingPanel;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部面板表现, 游戏直播
 */
public class PanelContainerPresenter extends ComponentPresenter<LivePanelContainer.IView>
        implements LivePanelContainer.IPresenter {
    private static final String TAG = "PanelContainerPresenter";

    @Nullable
    protected LiveRoomChatMsgManager mLiveRoomChatMsgManager;

    private BaseBottomPanel mSettingPanel;
    private BaseBottomPanel mMagicPanel;
    private LivePlusPanel mPlusPanel;

    private LivePlusPresenter mLivePlusPresenter;

    public PanelContainerPresenter(
            @NonNull IComponentController componentController,
            @Nullable LiveRoomChatMsgManager liveRoomChatMsgManager) {
        super(componentController);
        registerAction(LiveComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(LiveComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(LiveComponentController.MSG_ON_BACK_PRESSED);
        registerAction(LiveComponentController.MSG_SHOW_SETTING_PANEL);
        registerAction(LiveComponentController.MSG_SHOW_PLUS_PANEL);
        registerAction(LiveComponentController.MSG_SHOW_MAGIC_PANEL);
        mLiveRoomChatMsgManager = liveRoomChatMsgManager;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mPlusPanel != null) {
            mLivePlusPresenter.destroy();
            mLivePlusPresenter = null;
            mPlusPanel = null;
        }
    }

    private boolean showSettingPanel() {
        if (mSettingPanel == null) {
            mSettingPanel = new LiveSettingPanel((RelativeLayout) mView.getRealView());
        }
        return mView.showPanel(mSettingPanel);
    }

    private boolean showPlusPanel() {
        if (mPlusPanel == null) {
            mPlusPanel = new LivePlusPanel((RelativeLayout) mView.getRealView());
            mLivePlusPresenter = new LivePlusPresenter(mComponentController);
            mPlusPanel.setPresenter(mLivePlusPresenter);
            mLivePlusPresenter.setComponentView(mPlusPanel.getViewProxy());
        }
        return mView.showPanel(mPlusPanel);
    }

    private boolean showMagicPanel() {
        if (mMagicPanel == null) {
            mMagicPanel = new LiveMagicPanel((RelativeLayout) mView.getRealView());
        }
        return mView.showPanel(mMagicPanel);
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
                case LiveComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mView.onOrientation(false);
                    return true;
                case LiveComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mView.onOrientation(true);
                    return true;
                case LiveComponentController.MSG_SHOW_SETTING_PANEL:
                    return showSettingPanel();
                case LiveComponentController.MSG_SHOW_PLUS_PANEL:
                    return showPlusPanel();
                case LiveComponentController.MSG_SHOW_MAGIC_PANEL:
                    return showMagicPanel();
                case LiveComponentController.MSG_ON_BACK_PRESSED:
                    return mView.processBackPress();
                default:
                    break;

            }
            return false;
        }
    }
}
