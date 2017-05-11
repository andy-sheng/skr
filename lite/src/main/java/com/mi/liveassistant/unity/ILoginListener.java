package com.mi.liveassistant.unity;

/**
 * Created by yangli on 2017/5/10.
 */
interface ILoginListener {
    /**
     * 登录失败
     *
     * @param errCode 错误码
     */
    void onLoginFailed(int errCode);

    /**
     * 登录成功
     */
    void onLoginSuccess(String uid);
}
