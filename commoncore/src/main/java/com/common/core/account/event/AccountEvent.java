package com.common.core.account.event;

public class AccountEvent {
    /**
     * 给系统设置了一个 token 有效的账号，代表登陆上了
     * 但并不一定表示用户信息已经记载成功，要拿到用户信息
     * 请监听 {@link com.common.core.myinfo.event.MyUserInfoEvent.UserInfoChangeEvent}
     */
    public static class SetAccountEvent {
    }

    public static class LogoffAccountEvent {
        public static final int REASON_SELF_QUIT = 1; // 用户主动退出
        public static final int REASON_ACCOUNT_EXPIRED = 2;// 账号过期
        public int reason;

        public LogoffAccountEvent(int reason) {
            this.reason = reason;
        }
    }
}
