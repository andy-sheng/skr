package com.wali.live.watchsdk.ipc.service;

import com.wali.live.watchsdk.ipc.service.IMiLiveSdkEventCallback;
import com.wali.live.watchsdk.ipc.service.ThirdPartLoginData;

interface IMiLiveSdkService {
    void loginByMiAccountSso(int channelid, String packageName, String channelSecret, long miid, String serviceToken);

    void loginByMiAccountOAuth(int channelid, String packageName, String channelSecret, String code);

    void clearAccount(int channelid, String packageName, String channelSecret);

    void setEventCallBack(int channelid, IMiLiveSdkEventCallback callback);

    void checkService();

    void thirdPartLogin(String packageName, String channelSecret, in ThirdPartLoginData loginData);

    void getChannelLives(int channelid, String packageName, String channelSecret);

    void getFollowingList(int channelId, String packageName, String channelSecret,  boolean isBothWay, long timeStamp);
}
