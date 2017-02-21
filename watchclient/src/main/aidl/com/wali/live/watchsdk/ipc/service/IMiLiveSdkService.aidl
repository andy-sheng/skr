package com.wali.live.watchsdk.ipc.service;

import com.wali.live.watchsdk.ipc.service.IMiLiveSdkEventCallback;

interface IMiLiveSdkService {
    void loginByMiAccountSso(int channelid, String packageName, String channelSecret, long miid, String serviceToken);

    void loginByMiAccountOAuth(int channelid, String packageName, String channelSecret, String code);

    void clearAccount(int channelid, String packageName, String channelSecret);

    void setEventCallBack(int channelId, IMiLiveSdkEventCallback callback);
}
