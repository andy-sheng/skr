package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.watchsdk.component.view.GameInputView;

import static com.wali.live.componentwrapper.BaseSdkController.MSG_HIDE_GAME_BARRAGE;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_HIDE_GAME_INPUT;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_HIDE_INPUT_VIEW;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_GAME_BARRAGE;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_GAME_INPUT;

/**
 * Created by yangli on 2017/02/28.
 *
 * @module 游戏直播输入框表现, 观看
 */
public class GameInputPresenter extends InputPresenter<GameInputView.IView>
        implements GameInputView.IPresenter {
    private static final String TAG = "GameInputPresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public GameInputPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData) {
        super(controller, myRoomData);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_BACK_PRESSED);
        registerAction(MSG_HIDE_INPUT_VIEW);
        registerAction(MSG_SHOW_GAME_INPUT);
        registerAction(MSG_HIDE_GAME_INPUT);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    @Override
    public void showGameBarrage(boolean isShow) {
        if (isShow) {
            postEvent(MSG_SHOW_GAME_BARRAGE);
        } else {
            postEvent(MSG_HIDE_GAME_BARRAGE);
        }
    }

    @Override
    public void notifyInputViewShowed() {
        postEvent(MSG_INPUT_VIEW_SHOWED);
    }

    @Override
    public void notifyInputViewHidden() {
        postEvent(MSG_INPUT_VIEW_HIDDEN);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_SHOW_GAME_INPUT:
                mView.showSelf();
                return true;
            case MSG_HIDE_GAME_INPUT:
                mView.hideSelf();
                return true;
            case MSG_ON_BACK_PRESSED:
                return mView.processBackPress();
            case MSG_HIDE_INPUT_VIEW:
                return mView.hideInputView();
            default:
                break;
        }
        return false;
    }
}
