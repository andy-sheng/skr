package com.wali.live.watchsdk.component.presenter;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.event.KeyboardEvent;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.data.LastBarrage;
import com.mi.live.data.push.SendBarrageManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.event.EventClass;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.InputAreaView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by yangli on 17/02/20.
 *
 * @module 输入框表现
 */
public class InputAreaPresenter extends ComponentPresenter<InputAreaView.IView>
        implements InputAreaView.IPresenter {
    private static final String TAG = "InputAreaPresenter";

    private static final long CLEAR_BARRAGE_CACHE_INTERVAL = 12 * 60 * 60 * 1000;// 清理弹幕缓存的时间间隔

    private static final Map<String, LastBarrage> mLastBarrageMap = new HashMap<>();

    private String mInputContent;

    private boolean mViewIsShow;

    protected RoomBaseDataModel mMyRoomData;

    private static final int MSG_SEND_BARRAGE_COUNT_DOWN = 301;

    private MyUIHandler mUIHandler;

    private boolean mCanInput;

    private static class MyUIHandler extends Handler {
        private WeakReference<InputAreaPresenter> mPresenter;

        public MyUIHandler(InputAreaPresenter presenter){
            mPresenter = new WeakReference<>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            if(mPresenter.get() == null){
                return;
            }
            switch (msg.what) {
                case MSG_SEND_BARRAGE_COUNT_DOWN:
                    int interval = mPresenter.get().getSendBarrageInterval();
                    if(interval > 0){
                        mPresenter.get().mView.getInputView().setHint(com.base.global.GlobalData.app().getString(R.string.send_barrage_interval, interval));
                        mPresenter.get().mView.getInputView().setText("");
                        mPresenter.get().mUIHandler.sendEmptyMessageDelayed(MSG_SEND_BARRAGE_COUNT_DOWN,1000);
                        mPresenter.get().mCanInput = false;
                    }else{
                        mPresenter.get().mView.getInputView().setHint(R.string.empty_edittext_hint);
                        mPresenter.get().mCanInput = true;
//                        activity.mAllowInput = true;
                        if (!TextUtils.isEmpty(mPresenter.get().mInputContent)) {
                            mPresenter.get().mView.getInputView().setText(mPresenter.get().mInputContent);
                            mPresenter.get().mView.getInputView().setSelection(mPresenter.get().mInputContent.length());
                            mPresenter.get().mInputContent = "";
                        }
                    }
            }

        }
    }

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
        mCanInput = true;
        mUIHandler = new MyUIHandler(this);
        EventBus.getDefault().register(this);
        clearBarrageCache();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        mUIHandler.removeCallbacksAndMessages(null);
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

    /**
     * 处理发送弹幕频率限制更改事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.MsgRuleChangedEvent event) {
        if (event.getRoomId() != null && mMyRoomData.getRoomId().equals(event.getRoomId())) {
//            changeCommentBtnResource();

            MessageRule msgRule = new MessageRule();
            msgRule.setMessageRuleType(MessageRule.MessageRuleType.NORMAL);
            msgRule.setSpeakPeriod(event.getSpeakPeriod());
            msgRule.setUnrepeatable(event.isUnrepeatable());
            mMyRoomData.setmMsgRule(msgRule);

            LastBarrage lastBarrage = mLastBarrageMap.get(getBarrageCacheKey(mMyRoomData.getRoomId()));
            if (!event.isUnrepeatable()) {
                if (lastBarrage != null) {
                    lastBarrage.setLastSendContent(null);
                }
            }
            if (event.getSpeakPeriod() == 0) {
                if (lastBarrage != null && lastBarrage.getLastSendTime() > 0) {
                    lastBarrage.setLastSendTime(0);
                }
            } else if (event.getSpeakPeriod() > event.getOriSpeakPeriod()) {
                if (lastBarrage != null && lastBarrage.getLastSendTime() > 0 && (System.currentTimeMillis() - lastBarrage.getLastSendTime()) < event.getSpeakPeriod() * 1000) {
                    String text = mView.getInputView().getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        mInputContent = text;
                        mView.getInputView().setText("");
                    }
                }
            }
            if (mViewIsShow) {
                checkShowCountdownTimer();
            }
        }
    }

    /**
     * 获取发送弹幕的时间间隔
     *
     * @return
     */
    private int getSendBarrageInterval() {
        try {
            LastBarrage lastBarrage = mLastBarrageMap.get(getBarrageCacheKey(mMyRoomData.getRoomId()));
            long now = System.currentTimeMillis();
            if (mMyRoomData.getmMsgRule() != null && mMyRoomData.getmMsgRule().getSpeakPeriod() != 0 && lastBarrage.getLastSendTime() > 0) {
                if ((now - lastBarrage.getLastSendTime()) < mMyRoomData.getmMsgRule().getSpeakPeriod() * 1000) {
                    int interval = (int) (mMyRoomData.getmMsgRule().getSpeakPeriod() - (now - lastBarrage.getLastSendTime()) / 1000);
                    MyLog.w(TAG, "send barrage too frequent,interval:" + mMyRoomData.getmMsgRule().getSpeakPeriod() + "s now:" + now + " last send time:" + lastBarrage.getLastSendTime());
                    return interval;
                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        return 0;
    }

    /**
     * 显示输入倒计时
     */
    private void checkShowCountdownTimer() {
        try {
            if (getSendBarrageInterval() > 0) {
                if (mCanInput == true) {
                    mCanInput = false;
                    mUIHandler.sendEmptyMessageDelayed(MSG_SEND_BARRAGE_COUNT_DOWN,1000);

                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    /**
     * 清理弹幕缓存
     */
    private void clearBarrageCache() {
        try {
            Set<String> keySet = mLastBarrageMap.keySet();
            Date now = new Date();
            for (String key : keySet) {
                LastBarrage lastBarrage = mLastBarrageMap.get(key);
                if (lastBarrage != null && !key.equals(getBarrageCacheKey(mMyRoomData.getRoomId()))) {
                    if ((now.getTime() - lastBarrage.getCreateTime()) > CLEAR_BARRAGE_CACHE_INTERVAL) {
                        mLastBarrageMap.remove(key);
                        MyLog.w(TAG, "clear barrage cache,key:" + key);
                    }
                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, "clear barrage cache error", e);
        }
    }

    private String getBarrageCacheKey(String roomId){
        return roomId+UserAccountManager.getInstance().getUuidAsLong();
    }

    @Override
    public void sendBarrage(String msg, boolean isFlyBarrage) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (mMyRoomData != null && !mMyRoomData.canSpeak()) {
            ToastUtils.showToast(GlobalData.app(), R.string.can_not_speak);
            return;
        }

        LastBarrage lastBarrage = mLastBarrageMap.get(getBarrageCacheKey(mMyRoomData.getRoomId()));
        long sendTime = System.currentTimeMillis();
        if (mMyRoomData.getmMsgRule() != null && lastBarrage != null) {
            if (mMyRoomData.getmMsgRule().isUnrepeatable() && lastBarrage.getLastSendContent() != null
                    && msg.trim().equals(lastBarrage.getLastSendContent())) {
                MyLog.w(TAG, "send barrage repeated,last content:" + lastBarrage.getLastSendContent() + " body:" + msg.trim());
                ToastUtils.showToast(GlobalData.app().getApplicationContext(), R.string.send_barrage_repeated);
                return;
            }
            if (mMyRoomData.getmMsgRule().getSpeakPeriod() != 0 && lastBarrage.getLastSendTime() > 0) {
                if ((sendTime - lastBarrage.getLastSendTime()) < mMyRoomData.getmMsgRule().getSpeakPeriod() * 1000) {
                    return;
                }
            }
        }

        //把此次发送的弹幕存入缓存
        if (mMyRoomData.getmMsgRule() != null) {
            lastBarrage = lastBarrage == null ? new LastBarrage(new Date()) : lastBarrage;
            if (mMyRoomData.getmMsgRule().isUnrepeatable())
                lastBarrage.setLastSendContent(msg.trim());
            if (mMyRoomData.getmMsgRule().getSpeakPeriod() > 0)
                lastBarrage.setLastSendTime(sendTime);
            mLastBarrageMap.put(getBarrageCacheKey(mMyRoomData.getRoomId()), lastBarrage);
        }

        checkShowCountdownTimer();

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