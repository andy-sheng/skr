package com.wali.live.watchsdk.component.presenter;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

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
import com.thornbirds.component.presenter.ComponentPresenter;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.componentwrapper.BaseSdkController;
import com.wali.live.event.EventClass;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zyh on 2017/7/28.
 *
 * @module 游戏和秀场输入框的基类, 主要放房间管理，禁言频率限制等操作
 */
public abstract class InputPresenter<VIEW extends InputPresenter.IView>
        extends ComponentPresenter<VIEW, BaseSdkController> {
    protected static final String TAG = "InputPresenter";
    protected static final long CLEAR_BARRAGE_CACHE_INTERVAL = 12 * 60 * 60 * 1000;// 清理弹幕缓存的时间间隔
    protected static final int MSG_SEND_BARRAGE_COUNT_DOWN = 301;
    protected static final Map<String, LastBarrage> mLastBarrageMap = new HashMap<>();
    protected RoomBaseDataModel mMyRoomData;
    protected MyUIHandler mUIHandler;
    protected String mInputContent;
    protected boolean mCanInput;
    protected boolean mViewIsShow;

    public InputPresenter(
            @NonNull BaseSdkController controller,
            @NonNull RoomBaseDataModel myRoomData) {
        super(controller);
        mMyRoomData = myRoomData;
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
        if (mMyRoomData.getMsgRule() != null && lastBarrage != null) {
            if (mMyRoomData.getMsgRule().isUnrepeatable() && lastBarrage.getLastSendContent() != null
                    && msg.trim().equals(lastBarrage.getLastSendContent())) {
                MyLog.w(TAG, "send barrage repeated,last content:" + lastBarrage.getLastSendContent() + " body:" + msg.trim());
                ToastUtils.showToast(GlobalData.app().getApplicationContext(), R.string.send_barrage_repeated);
                return;
            }
            if (mMyRoomData.getMsgRule().getSpeakPeriod() != 0 && lastBarrage.getLastSendTime() > 0) {
                if ((sendTime - lastBarrage.getLastSendTime()) < mMyRoomData.getMsgRule().getSpeakPeriod() * 1000) {
                    return;
                }
            }
        }
        //把此次发送的弹幕存入缓存
        if (mMyRoomData.getMsgRule() != null) {
            lastBarrage = lastBarrage == null ? new LastBarrage(new Date()) : lastBarrage;
            if (mMyRoomData.getMsgRule().isUnrepeatable())
                lastBarrage.setLastSendContent(msg.trim());
            if (mMyRoomData.getMsgRule().getSpeakPeriod() > 0)
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

    /**
     * 获取发送弹幕的时间间隔
     *
     * @return
     */
    private int getSendBarrageInterval() {
        try {
            LastBarrage lastBarrage = mLastBarrageMap.get(getBarrageCacheKey(mMyRoomData.getRoomId()));
            long now = System.currentTimeMillis();
            if (mMyRoomData.getMsgRule() != null && mMyRoomData.getMsgRule().getSpeakPeriod() != 0 && lastBarrage.getLastSendTime() > 0) {
                if ((now - lastBarrage.getLastSendTime()) < mMyRoomData.getMsgRule().getSpeakPeriod() * 1000) {
                    int interval = (int) (mMyRoomData.getMsgRule().getSpeakPeriod() - (now - lastBarrage.getLastSendTime()) / 1000);
                    MyLog.w(TAG, "send barrage too frequent,interval:" + mMyRoomData.getMsgRule().getSpeakPeriod() + "s now:" + now + " last send time:" + lastBarrage.getLastSendTime());
                    return interval;
                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        return 0;
    }

    /**
     * 清理弹幕缓存
     */
    protected void clearBarrageCache() {
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

    private String getBarrageCacheKey(String roomId) {
        return roomId + UserAccountManager.getInstance().getUuidAsLong();
    }

    /**
     * 显示输入倒计时
     */
    protected void checkShowCountdownTimer() {
        try {
            if (getSendBarrageInterval() > 0) {
                if (mCanInput == true) {
                    mCanInput = false;
                    mUIHandler.sendEmptyMessageDelayed(MSG_SEND_BARRAGE_COUNT_DOWN, 1000);

                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
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
            mMyRoomData.setMsgRule(msgRule);

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

    public interface IPresenter {
        /**
         * 发送消息
         */
        void sendBarrage(String msg, boolean isFlyBarrage);
    }

    public interface IView extends IViewProxy<View> {
        /**
         * 获取输入框
         */
        EditText getInputView();

        /**
         * 键盘弹起
         */
        void onKeyboardShowed(int keyboardHeight);

        /**
         * 键盘隐藏
         */
        void onKeyboardHidden();
    }

    private static class MyUIHandler extends Handler {
        private WeakReference<InputPresenter<? extends IView>> mPresenter;

        public MyUIHandler(InputPresenter presenter) {
            mPresenter = new WeakReference<InputPresenter<? extends IView>>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            InputPresenter<? extends IView> inputPresenter = mPresenter.get();
            if (inputPresenter == null) {
                return;
            }
            switch (msg.what) {
                case MSG_SEND_BARRAGE_COUNT_DOWN:
                    int interval = inputPresenter.getSendBarrageInterval();
                    if (interval > 0) {
                        inputPresenter.mView.getInputView().setHint(GlobalData.app().getString(R.string.send_barrage_interval, interval));
                        inputPresenter.mView.getInputView().setText("");
                        inputPresenter.mUIHandler.sendEmptyMessageDelayed(MSG_SEND_BARRAGE_COUNT_DOWN, 1000);
                        inputPresenter.mCanInput = false;
                    } else {
                        inputPresenter.mView.getInputView().setHint(R.string.empty_edittext_hint);
                        inputPresenter.mCanInput = true;
                        if (!TextUtils.isEmpty(mPresenter.get().mInputContent)) {
                            inputPresenter.mView.getInputView().setText(mPresenter.get().mInputContent);
                            inputPresenter.mView.getInputView().setSelection(mPresenter.get().mInputContent.length());
                            inputPresenter.mInputContent = "";
                        }
                    }
            }
        }
    }
}
