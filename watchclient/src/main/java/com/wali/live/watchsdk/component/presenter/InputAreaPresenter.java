package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.event.KeyboardEvent;
import com.base.log.MyLog;
import com.mi.live.data.push.SendBarrageManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.InputAreaView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by yangli on 17/02/20.
 *
 * @module 输入框表现
 */
public class InputAreaPresenter extends ComponentPresenter<InputAreaView.IView>
        implements InputAreaView.IPresenter {
    private static final String TAG = "InputAreaPresenter";

    protected RoomBaseDataModel mMyRoomData;

    public InputAreaPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData) {
        super(componentController);
        mMyRoomData = myRoomData;
        registerAction(ComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(ComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(ComponentController.MSG_ON_BACK_PRESSED);
        registerAction(ComponentController.MSG_SHOW_INPUT_VIEW);
        registerAction(ComponentController.MSG_HIDE_INPUT_VIEW);
        registerAction(ComponentController.MSG_SHOW_BARRAGE_SWITCH);
        registerAction(ComponentController.MSG_HIDE_BARRAGE_SWITCH);
        EventBus.getDefault().register(this);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.w(TAG, "KeyboardEvent eventType=" + event.eventType);
        if (mView == null) {
            MyLog.e(TAG, "KeyboardEvent but mView is null");
            return;
        }
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE_ALWAYS_SEND:
                int keyboardHeight = Integer.parseInt(String.valueOf(event.obj1));
                mView.onKeyboardShowed(keyboardHeight);
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                mView.onKeyboardHidden();
                break;
        }
    }

    @Override
    public void sendBarrage(String msg, boolean isFlyBarrage) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (mMyRoomData == null) {
            MyLog.e("sendBarrage but mMyRoomData is null");
            return;
        }
        BarrageMsg barrageMsg = SendBarrageManager.createBarrage(BarrageMsgType.B_MSG_TYPE_TEXT,
                msg, mMyRoomData.getRoomId(), mMyRoomData.getUid(), System.currentTimeMillis(), null);
        SendBarrageManager
                .sendBarrageMessageAsync(barrageMsg)
                .subscribe();
        SendBarrageManager.pretendPushBarrage(barrageMsg);
    }

    @Override
    public void notifyInputViewShowed() {
        mComponentController.onEvent(ComponentController.MSG_INPUT_VIEW_SHOWED);
    }

    @Override
    public void notifyInputViewHidden() {
        mComponentController.onEvent(ComponentController.MSG_INPUT_VIEW_HIDDEN);
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
                    return mView.showInputView();
                case ComponentController.MSG_HIDE_INPUT_VIEW:
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