package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.event.KeyboardEvent;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.push.SendBarrageManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.view.GameInputView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by yangli on 2017/02/28.
 *
 * @module 游戏直播输入框表现, 观看
 */
public class GameInputPresenter extends ComponentPresenter<GameInputView.IView>
        implements GameInputView.IPresenter {
    private static final String TAG = "GameInputPresenter";

    protected RoomBaseDataModel mMyRoomData;

    public GameInputPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData) {
        super(componentController);
        mMyRoomData = myRoomData;
        registerAction(WatchComponentController.MSG_ON_BACK_PRESSED);
        registerAction(WatchComponentController.MSG_HIDE_INPUT_VIEW);
        registerAction(WatchComponentController.MSG_SHOW_GAME_INPUT);
        registerAction(WatchComponentController.MSG_HIDE_GAME_INPUT);
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
            default:
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
        if (!mMyRoomData.canSpeak()) {
            ToastUtils.showToast(GlobalData.app(), R.string.can_not_speak);
            return;
        }
        // 检查发送频率限制
        if (mMyRoomData.getMsgRule() != null && mMyRoomData.getMsgRule().getSpeakPeriod() == Integer.MAX_VALUE) {
            return;
        }
        String body = SmileyParser.getInstance()
                .convertString(msg, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString();
        BarrageMsg barrageMsg = SendBarrageManager.createBarrage(BarrageMsgType.B_MSG_TYPE_TEXT,
                body, mMyRoomData.getRoomId(), mMyRoomData.getUid(), System.currentTimeMillis(), null);
        SendBarrageManager
                .sendBarrageMessageAsync(barrageMsg)
                .subscribe();
        SendBarrageManager.pretendPushBarrage(barrageMsg);
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
