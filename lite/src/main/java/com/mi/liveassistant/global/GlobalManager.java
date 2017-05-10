package com.mi.liveassistant.global;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.event.AccountEvent;
import com.mi.liveassistant.global.callback.IAccountListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by lan on 17/5/10.
 *
 * @description 全局管理，用于通知上层应用全局的消息，比如登录失败的状态
 */
public enum GlobalManager {
    INSTANCE;

    private static final String TAG = GlobalManager.class.getSimpleName();

    private IAccountListener mAccountListener;

    public void setAccountListener(IAccountListener listener) {
        mAccountListener = listener;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.LogOffEvent event) {
        if (event == null) {
            return;
        }
        MyLog.d(TAG, "event type=" + event.getEventType());
        switch (event.getEventType()) {
            case AccountEvent.LogOffEvent.EVENT_TYPE_FORBIDDEN:
                mAccountListener.forbidAccount();
                break;
            case AccountEvent.LogOffEvent.EVENT_TYPE_EXPIRE_LOGOFF:
                mAccountListener.logoffAccount();
                break;
            case AccountEvent.LogOffEvent.EVENT_TYPE_KICK:
                mAccountListener.kickAccount();
                break;
        }
    }
}
