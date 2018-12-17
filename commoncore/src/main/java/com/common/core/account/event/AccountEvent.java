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
    }
}
