package com.wali.live.watchsdk.ipc.service;

import com.wali.live.watchsdk.ipc.service.IMiLiveSdkEventCallback;

interface IMiLiveSdkService {
    void openWatch(int channelId, String packageName, long playerId, String liveId, String videoUrl);

    void openReplay(int channelId, String packageName, long playerId, String liveId, String videoUrl);

    void openGameLive();

    void loginByMiAccountSso(int channelid, String packageName, long miid, String serviceToken);

    void loginByMiAccountOAuth(int channelid, String packageName, String code);

    void clearAccount(int channelid, String packageName);

    void setEventCallBack(int channelid, IMiLiveSdkEventCallback callback);
}
