package com.wali.live.livesdk.live.livegame.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.wali.live.livesdk.live.component.ComponentController;
import com.wali.live.livesdk.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.livegame.LiveComponentController;
import com.wali.live.livesdk.live.livegame.view.LiveBottomButton;
import com.wali.live.livesdk.live.presenter.GameLivePresenter;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部按钮表现, 游戏直播
 */
public class BottomButtonPresenter extends
        ComponentPresenter<LiveBottomButton.IView> implements LiveBottomButton.IPresenter {
    private static final String TAG = "BottomButtonPresenter";

    @Nullable
    private GameLivePresenter mGameLivePresenter;

    public BottomButtonPresenter(
            @NonNull IComponentController componentController,
            @Nullable GameLivePresenter gameLivePresenter) {
        super(componentController);
        registerAction(ComponentController.MSG_ON_ORIENTATION);
        mGameLivePresenter = gameLivePresenter;
    }

    @Override
    public void showInputView() {
        mComponentController.onEvent(ComponentController.MSG_SHOW_INPUT_VIEW);
    }

    @Override
    public void showSettingPanel() {
        mComponentController.onEvent(ComponentController.MSG_SHOW_SETTING_PANEL);
    }

    @Override
    public void muteAudio(boolean isMute) {
        if (mGameLivePresenter != null) {
            mGameLivePresenter.muteMic(isMute);
        }
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
                case LiveComponentController.MSG_ON_ORIENTATION:
                    if (params != null && mView != null) {
                        Boolean isLandscape = params.fetchItem();
                        if (isLandscape != null) {
                            mView.onOrientation(isLandscape);
                            return true;
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
