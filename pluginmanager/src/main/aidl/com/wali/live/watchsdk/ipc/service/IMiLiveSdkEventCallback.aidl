package com.wali.live.watchsdk.ipc.service;

interface IMiLiveSdkEventCallback {
    void onEventLogin(int code);

    void onEventLogoff(int code);

    void onEventWantLogin();

    void onEventVerifyFailure(int code);

    void onEventOtherAppActive();
}
