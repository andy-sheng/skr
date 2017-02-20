package com.mi.live.data.account.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 16/11/23.
 */
public class AccountEventController {
    /**
     * 初始化账号完成事件
     */
    public static class InitAccountFinishEvent {
        public InitAccountFinishEvent() {
        }
    }

    public static void onActionInitAccountFinish() {
        InitAccountFinishEvent event = new InitAccountFinishEvent();
        EventBus.getDefault().post(event);
    }

    /**
     * 踢下线事件
     */
    public static class LogOffEvent {
        public static final int EVENT_TYPE_NONE = 0;
        public static final int EVENT_TYPE_KICK = 1;
        public static final int EVENT_TYPE_NORMAL_LOGOFF = 2;
        public static final int EVENT_TYPE_ACCOUNT_FORBIDDEN = 3;
        private int eventType = EVENT_TYPE_NONE;

        public LogOffEvent(int type) {
            this.eventType = type;
        }

        public int getEventType() {
            return eventType;
        }

        public void setEventType(int type) {
            this.eventType = type;
        }
    }

    public static void onActionLogOff(int type) {
        LogOffEvent event = new LogOffEvent(type);
        EventBus.getDefault().post(event);
    }

    /**
     * 登录事件
     */
    public static class LoginEvent {
        public static final int EVENT_TYPE_NONE = 0;
        public static final int EVENT_TYPE_LOGIN_CANCEL = 1;
        public static final int EVENT_TYPE_LOGIN_SUCCESS = 2;
        public static final int EVENT_TYPE_LOGIN_FAILED = 3;
        public static final int EVENT_TYPE_LOGIN_EXCEPTION = 4;
        private int eventType = EVENT_TYPE_NONE;

        public LoginEvent(int type) {
            this.eventType = type;
        }

        public int getEventType() {
            return eventType;
        }

        public void setEventType(int type) {
            this.eventType = type;
        }
    }

    // 通知用户信息修改
    public static void onActionLogin(int type) {
        LoginEvent event = new LoginEvent(type);
        EventBus.getDefault().post(event);
    }
}
