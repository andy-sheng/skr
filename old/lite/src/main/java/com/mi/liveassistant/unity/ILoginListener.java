package com.mi.liveassistant.unity;

/**
 * Created by yangli on 2017/5/10.
 */
interface ILoginListener {
    /**
     * 登录失败
     *
     * @param errCode 错误码
     * @param errMsg  错误描述
     */
    void onLoginFailed(int errCode, String errMsg);

    /**
     * 登录成功
     *
     * @parsm uid 小米直播ID
     */
    void onLoginSuccess(long uid);
}
