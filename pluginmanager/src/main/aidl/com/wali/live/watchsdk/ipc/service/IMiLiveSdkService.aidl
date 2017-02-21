package com.wali.live.watchsdk.ipc.service;

import com.wali.live.watchsdk.ipc.service.IMiLiveSdkEventCallback;

interface IMiLiveSdkService {
    void loginByMiAccountSso(int channelid, String packageName, long miid, String serviceToken);

    void loginByMiAccountOAuth(int channelid, String packageName, String code);

    void clearAccount(int channelid, String packageName);

    void setEventCallBack(int channelid, IMiLiveSdkEventCallback callback);
}
