package com.wali.live.livesdk.live.livegame.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.component.view.panel.BaseSettingPanel;
import com.wali.live.livesdk.live.livegame.LiveComponentController;
import com.wali.live.livesdk.live.livegame.view.LivePanelContainer;
import com.wali.live.livesdk.live.livegame.view.panel.GameSettingPanel;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部面板表现, 游戏直播
 */
public class PanelContainerPresenter extends
        ComponentPresenter<LivePanelContainer.IView> implements LivePanelContainer.IPresenter {
    private static final String TAG = "PanelContainerPresenter";

    @Nullable
    protected LiveRoomChatMsgManager mLiveRoomChatMsgManager;

    public PanelContainerPresenter(
            @NonNull IComponentController componentController,
            @Nullable LiveRoomChatMsgManager liveRoomChatMsgManager) {
        super(componentController);
        registerAction(LiveComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(LiveComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(LiveComponentController.MSG_ON_BACK_PRESSED);
        registerAction(LiveComponentController.MSG_SHOW_SETTING_PANEL);
        mLiveRoomChatMsgManager = liveRoomChatMsgManager;
    }

    @Nullable
    @Override
    public BaseSettingPanel createSettingPanel() {
        if (mView == null || mView.getRealView() == null) {
            return null;
        }
        return new GameSettingPanel((RelativeLayout) mView.getRealView(), mLiveRoomChatMsgManager);
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
                    mView.showSettingPanel();
                    return true;
                case LiveComponentController.MSG_ON_BACK_PRESSED:
                    return mView.processBackPress();
                default:
                    break;

            }
            return false;
        }
    }
}
