package com.wali.live.livesdk.live.livegame.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
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
    private RoomBaseDataModel mMyRoomData;

    public BottomButtonPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData,
            @Nullable GameLivePresenter gameLivePresenter) {
        super(componentController);
        registerAction(LiveComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(LiveComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(LiveComponentController.MSG_ON_ACTIVITY_RESUMED);
        mMyRoomData = myRoomData;
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

    @Override
    public void showShareView() {
        mComponentController.onEvent(LiveComponentController.MSG_SHOW_SHARE_PANEL);
    }

    @Override
    public boolean isEnableShare() {
        return mMyRoomData != null ? mMyRoomData.getEnableShare() : false;
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
                case LiveComponentController.MSG_ON_ACTIVITY_RESUMED:
                    if (mGameLivePresenter != null) {
                        mView.updateMuteBtn(mGameLivePresenter.isMuteMic());
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
