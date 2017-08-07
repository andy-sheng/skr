package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.watchsdk.component.view.InputAreaView;

import static com.wali.live.component.BaseSdkController.MSG_HIDE_BARRAGE_SWITCH;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_INPUT_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_BARRAGE_SWITCH;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_INPUT_VIEW;

/**
 * Created by yangli on 17/02/20.
 *
 * @module 输入框表现
 */
public class InputAreaPresenter extends InputPresenter<InputAreaView.IView>
        implements InputAreaView.IPresenter {
    private static final String TAG = "InputAreaPresenter";

    private int mMinHeightLand;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public InputAreaPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData,
            boolean isWatchState) {
        super(controller, myRoomData);
        setMinHeightLand(isWatchState);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_BACK_PRESSED);
        registerAction(MSG_SHOW_INPUT_VIEW);
        registerAction(MSG_HIDE_INPUT_VIEW);
        // TODO 后续完善飘屏弹幕逻辑之后，再开启飘屏弹幕功能 YangLi
//        registerAction(MSG_SHOW_BARRAGE_SWITCH);
//        registerAction(MSG_HIDE_BARRAGE_SWITCH);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    private void setMinHeightLand(boolean isWatchState) {
        if (isWatchState) {
            mMinHeightLand = DisplayUtils.dip2px(38f + 6.67f);
        } else {
            mMinHeightLand = DisplayUtils.dip2px(6.67f);
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
    public int getMinHeightLand() {
        return mMinHeightLand;
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mView.onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mView.onOrientation(true);
                return true;
            case MSG_ON_BACK_PRESSED:
                return mView.processBackPress();
            case MSG_SHOW_INPUT_VIEW:
                mViewIsShow = true;
                checkShowCountdownTimer();
                return mView.showInputView();
            case MSG_HIDE_INPUT_VIEW:
                mViewIsShow = false;
                return mView.hideInputView();
            case MSG_SHOW_BARRAGE_SWITCH:
                mView.enableFlyBarrage(true);
                return true;
            case MSG_HIDE_BARRAGE_SWITCH:
                mView.enableFlyBarrage(false);
                return true;
            default:
                break;
        }
        return false;
    }
}