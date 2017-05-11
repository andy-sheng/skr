package com.mi.liveassistant.account.callback;

/**
 * Created by lan on 17/5/9.
 */
public interface IAccountCallback {
    void notifyFail(int errCode);

    void notifySuccess(String uid);
}
