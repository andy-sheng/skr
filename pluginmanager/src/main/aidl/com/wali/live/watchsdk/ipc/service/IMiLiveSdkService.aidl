package com.wali.live.watchsdk.ipc.service;

import com.wali.live.watchsdk.ipc.service.IMiLiveSdkEventCallback;

interface IMiLiveSdkService {
    void openWatch(int channelId, String packageName, long playerId, String liveId, String videoUrl);

    void openReplay(int channelId, String packageName, long playerId, String liveId, String videoUrl);

    void openGameLive();

    void loginByMiAccountSso(int channelid, long miid, String serviceToken);

    void loginByMiAccountOAuth(int channelid, String code);

    void clearAccount(int channelid);

    void setEventCallBack(int channelid, IMiLiveSdkEventCallback callback);
}
