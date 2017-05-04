package com.wali.live.watchsdk.ipc.service;
import com.wali.live.watchsdk.ipc.service.LiveInfo;
import com.wali.live.watchsdk.ipc.service.UserInfo;
import com.wali.live.watchsdk.ipc.service.ShareInfo;

interface IMiLiveSdkEventCallback {
    void onEventLogin(int code);

    void onEventLogoff(int code);

    void onEventWantLogin();

    void onEventVerifyFailure(int code);

    void onEventOtherAppActive();

    void onEventGetRecommendLives(int errCode, in List<LiveInfo> liveInfos);

    void onEventGetFollowingUserList(int errCode, in List<UserInfo> userInfos, int total ,long timeStamp);

    void onEventGetFollowingLiveList(int errCode, in List<LiveInfo> liveInfos);

    void onEventShare(in ShareInfo shareInfo);

    void onEventFollow(long uuid);
}
