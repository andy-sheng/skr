package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.view.WatchPanelContainer;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部面板表现, 游戏直播
 */
public class PanelContainerPresenter extends
        ComponentPresenter<WatchPanelContainer.IView> implements WatchPanelContainer.IPresenter {
    private static final String TAG = "PanelContainerPresenter";

    @Nullable
    protected LiveRoomChatMsgManager mLiveRoomChatMsgManager;

    public PanelContainerPresenter(
            @NonNull IComponentController componentController,
            @Nullable LiveRoomChatMsgManager liveRoomChatMsgManager) {
        super(componentController);
        registerAction(WatchComponentController.MSG_ON_ORIENTATION);
        registerAction(WatchComponentController.MSG_ON_BACK_PRESSED);
        mLiveRoomChatMsgManager = liveRoomChatMsgManager;
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
                case WatchComponentController.MSG_ON_ORIENTATION:
                    if (params != null && mView != null) {
                        Boolean isLandscape = params.fetchItem();
                        if (isLandscape != null) {
                            mView.onOrientation(isLandscape);
                            return true;
                        }
                    }
                    break;
                case WatchComponentController.MSG_ON_BACK_PRESSED:
                    if (mView != null) {
                        return mView.processBackPress();
                    }
                    break;
                default:
                    break;

            }
            return false;
        }
    }
}
