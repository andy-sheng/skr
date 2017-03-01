package com.wali.live.watchsdk.relation;

import com.base.log.MyLog;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.wali.live.base.BaseEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 用户关系管理类，目前的功能只是简单在登陆后，发送用户消息更新事件，更新下观看界面的用户信息
 */
public enum UserRelationManager {

    INSTANCE;

    private static final String TAG = UserRelationManager.class.getSimpleName();

    private long mLastSyncTime;

    UserRelationManager() {
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(MiLinkEvent.StatusLogined event) {
        if (event != null) {
            long time = System.currentTimeMillis();
            if (MiLinkClientAdapter.getsInstance().isMiLinkLogined()
                    && time - mLastSyncTime > 30 * 1000) {
                MyLog.d(TAG, "StatusLogined");
                EventBus.getDefault().post(new BaseEvent.UserActionEvent(
                        BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_REFRESH_USER_RELATION, null, null));
                mLastSyncTime = time;
            }
        }
    }
}

