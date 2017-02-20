// IMiLiveSdkEventCallback.aidl
package com.wali.live.watchsdk.ipc.service;

// Declare any non-default types here with import statements

/**
* 保持和 livesdk 同步
* */
interface IMiLiveSdkEventCallback {

    void onEventLogin(int code);

    void onEventLogoff(int code);

    void onEventWantLogin();
}
