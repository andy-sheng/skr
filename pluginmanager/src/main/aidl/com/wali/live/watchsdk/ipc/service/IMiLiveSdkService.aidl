// IMiLiveSdkService.aidl
package com.wali.live.watchsdk.ipc.service;

import com.wali.live.watchsdk.ipc.service.IMiLiveSdkEventCallback;
// Declare any non-default types here with import statements

/**
* 保持和 livesdk 同步
* */
interface IMiLiveSdkService {
    void openWatch(long playerId, String liveId, String videoUrl);

    void loginByMiAccountSso(int channelid, long miid, String serviceToken);

    void loginByMiAccountOAuth(int channelid, String code);

    void clearAccount(int channelid);

    void setEventCallBack(int channelid, IMiLiveSdkEventCallback callback);
}
