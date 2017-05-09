package com.mi.liveassistant.login;

/**
 * Created by lan on 17/5/9.
 */
public interface ILoginCallback {
    void notifyFail(int errCode);

    void notifySuccess();
}
