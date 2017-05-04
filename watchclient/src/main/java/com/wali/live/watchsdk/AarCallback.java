package com.wali.live.watchsdk;

import com.base.log.MyLog;
import com.wali.live.watchsdk.ipc.service.LiveInfo;
import com.wali.live.watchsdk.ipc.service.ShareInfo;
import com.wali.live.watchsdk.ipc.service.UserInfo;

import java.util.List;

/**
 * Created by zyh on 2017/4/19.
 */

public class AarCallback implements IMiLiveSdk.ICallback, IMiLiveSdk.IChannelCallback, IMiLiveSdk.IFollowingListCallback, IMiLiveSdk.IFollowingLivesCallback {
    private IMiLiveSdk.ICallback mCallback;
    private IMiLiveSdk.IChannelCallback mChannelCallback;
    private IMiLiveSdk.IFollowingListCallback mFollowingListCallback;
    private IMiLiveSdk.IFollowingLivesCallback mFollowingLivesCallback;

    public void setCallback(IMiLiveSdk.ICallback callback) {
        mCallback = callback;
    }

    public void setChannelCallback(IMiLiveSdk.IChannelCallback callback) {
        mChannelCallback = callback;
    }

    public void setFollowingListCallback(IMiLiveSdk.IFollowingListCallback followingListCallback) {
        mFollowingListCallback = followingListCallback;
    }

    public void setFollowingLivesCallback(IMiLiveSdk.IFollowingLivesCallback followingLivesCallback) {
        mFollowingLivesCallback = followingLivesCallback;
    }

    @Override
    public void notifyLogin(int errCode) {
        if (mCallback != null) {
            mCallback.notifyLogin(errCode);
        }

    }

    @Override
    public void notifyLogoff(int errCode) {
        if (mCallback != null) {
            mCallback.notifyLogoff(errCode);
        }
    }

    @Override
    public void notifyWantLogin() {
        if (mCallback != null) {
            mCallback.notifyWantLogin();
        }
    }

    @Override
    public void notifyVerifyFailure(int errCode) {
        if (mCallback != null) {
            mCallback.notifyVerifyFailure(errCode);
        }
    }

    @Override
    public void notifyOtherAppActive() {
        if (mCallback != null) {
            mCallback.notifyOtherAppActive();
        }
    }

    @Override
    public void notifyWantShare(ShareInfo shareInfo) {
        if (mCallback != null) {
            mCallback.notifyWantShare(shareInfo);
        }
    }

    @Override
    public void notifyGetChannelLives(int errCode, List<LiveInfo> liveInfos) {
        if (mChannelCallback != null) {
            mChannelCallback.notifyGetChannelLives(errCode, liveInfos);
        }
    }

    @Override
    public void notifyGetFollowingList(int errCode, List<UserInfo> userInfos, int total, long timeStamp) {
        if (mFollowingListCallback != null) {
            mFollowingListCallback.notifyGetFollowingList(errCode, userInfos, total, timeStamp);
        }
    }

    @Override
    public void notifyGetFollowingLives(int errCode, List<LiveInfo> liveInfos) {
        if (mFollowingLivesCallback != null) {
            mFollowingLivesCallback.notifyGetFollowingLives(errCode, liveInfos);
        }
    }
}
