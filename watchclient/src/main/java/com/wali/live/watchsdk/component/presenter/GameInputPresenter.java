package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.view.GameInputView;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yangli on 2017/02/28.
 *
 * @module 游戏直播输入框表现, 观看
 */
public class GameInputPresenter extends InputPresenter<GameInputView.IView>
        implements GameInputView.IPresenter {
    private static final String TAG = "GameInputPresenter";

    public GameInputPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData) {
        super(componentController, myRoomData);

        registerAction(WatchComponentController.MSG_ON_BACK_PRESSED);
        registerAction(WatchComponentController.MSG_HIDE_INPUT_VIEW);
        registerAction(WatchComponentController.MSG_SHOW_GAME_INPUT);
        registerAction(WatchComponentController.MSG_HIDE_GAME_INPUT);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void showGameBarrage(boolean isShow) {
        if (isShow) {
            mComponentController.onEvent(WatchComponentController.MSG_SHOW_GAME_BARRAGE);
        } else {
            mComponentController.onEvent(WatchComponentController.MSG_HIDE_GAME_BARRAGE);
        }
    }

    @Override
    public void notifyInputViewShowed() {
        mComponentController.onEvent(WatchComponentController.MSG_INPUT_VIEW_SHOWED);
    }

    @Override
    public void notifyInputViewHidden() {
        mComponentController.onEvent(WatchComponentController.MSG_INPUT_VIEW_HIDDEN);
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
                case WatchComponentController.MSG_SHOW_GAME_INPUT:
                    mView.showSelf();
                    return true;
                case WatchComponentController.MSG_HIDE_GAME_INPUT:
                    mView.hideSelf();
                    return true;
                case WatchComponentController.MSG_ON_BACK_PRESSED:
                    return mView.processBackPress();
                case WatchComponentController.MSG_HIDE_INPUT_VIEW:
                    return mView.hideInputView();
                default:
                    break;
            }
            return false;
        }
    }
}
