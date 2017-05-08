package com.wali.live.watchsdk.ipc.service;

import com.wali.live.watchsdk.ipc.service.IMiLiveSdkEventCallback;
import com.wali.live.watchsdk.ipc.service.ThirdPartLoginData;

interface IMiLiveSdkService {
    void loginByMiAccountSso(int channelId, String packageName, String channelSecret, long miid, String serviceToken);

    void loginByMiAccountOAuth(int channelId, String packageName, String channelSecret, String code);

    void clearAccount(int channelId, String packageName, String channelSecret);

    void setEventCallBack(int channelId, IMiLiveSdkEventCallback callback);

    void checkService();

    void thirdPartLogin(String packageName, String channelSecret, in ThirdPartLoginData loginData);

    void getChannelLives(int channelId, String packageName, String channelSecret);

    void getFollowingUserList(int channelId, String packageName, String channelSecret,  boolean isBothWay, long timeStamp);

    void getFollowingLiveList(int channelId, String packageName, String channelSecret);

    void notifyShare(int channelId, String packageName, String channelSecret, boolean success, int type);
}
