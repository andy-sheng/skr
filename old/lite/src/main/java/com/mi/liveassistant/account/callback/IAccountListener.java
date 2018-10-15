package com.mi.liveassistant.account.callback;

/**
 * Created by lan on 17/5/10.
 */
public interface IAccountListener {
    /**
     * 账号被封禁
     */
    void forbidAccount();

    /**
     * serviceToken过期导致的账号退出
     */
    void logoffAccount();

    /**
     * 账号被踢下线
     */
    void kickAccount();
}
