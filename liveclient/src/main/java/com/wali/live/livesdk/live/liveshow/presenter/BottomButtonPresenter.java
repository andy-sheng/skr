package com.wali.live.livesdk.live.liveshow.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.liveshow.LiveComponentController;
import com.wali.live.livesdk.live.liveshow.view.LiveBottomButton;
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
        registerAction(LiveComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(LiveComponentController.MSG_ON_ORIENT_LANDSCAPE);
        mGameLivePresenter = gameLivePresenter;
    }

    @Override
    public void showInputView() {
        mComponentController.onEvent(LiveComponentController.MSG_SHOW_INPUT_VIEW);
    }

    @Override
    public void showSettingPanel() {
        mComponentController.onEvent(LiveComponentController.MSG_SHOW_SETTING_PANEL);
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
                default:
                    break;
            }
            return false;
        }
    }
}
