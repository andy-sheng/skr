package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.ComponentController;
import com.wali.live.watchsdk.component.view.InputAreaView;

/**
 * Created by yangli on 17/02/20.
 *
 * @module 输入框表现
 */
public class InputAreaPresenter extends InputPresenter<InputAreaView.IView>
        implements InputAreaView.IPresenter {
    private static final String TAG = "InputAreaPresenter";
    private int mMinHeightLand;


    public InputAreaPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData,
            boolean isWatchState) {
        super(componentController, myRoomData);
        setMinHeightLand(isWatchState);
        registerAction(ComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(ComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(ComponentController.MSG_ON_BACK_PRESSED);
        registerAction(ComponentController.MSG_SHOW_INPUT_VIEW);
        registerAction(ComponentController.MSG_HIDE_INPUT_VIEW);
        // TODO 后续完善飘屏弹幕逻辑之后，再开启飘屏弹幕功能 YangLi
//        registerAction(ComponentController.MSG_SHOW_BARRAGE_SWITCH);
//        registerAction(ComponentController.MSG_HIDE_BARRAGE_SWITCH);
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
        mComponentController.onEvent(ComponentController.MSG_INPUT_VIEW_SHOWED);
    }

    @Override
    public void notifyInputViewHidden() {
        mComponentController.onEvent(ComponentController.MSG_INPUT_VIEW_HIDDEN);
    }

    @Override
    public int getMinHeightLand() {
        return mMinHeightLand;
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
                case ComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mView.onOrientation(false);
                    return true;
                case ComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mView.onOrientation(true);
                    return true;
                case ComponentController.MSG_ON_BACK_PRESSED:
                    return mView.processBackPress();
                case ComponentController.MSG_SHOW_INPUT_VIEW:
                    mViewIsShow = true;
                    checkShowCountdownTimer();
                    return mView.showInputView();
                case ComponentController.MSG_HIDE_INPUT_VIEW:
                    mViewIsShow = false;
                    return mView.hideInputView();
                case ComponentController.MSG_SHOW_BARRAGE_SWITCH:
                    mView.enableFlyBarrage(true);
                    return true;
                case ComponentController.MSG_HIDE_BARRAGE_SWITCH:
                    mView.enableFlyBarrage(false);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}