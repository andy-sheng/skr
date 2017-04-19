package com.wali.live.watchsdk;

import com.wali.live.watchsdk.ipc.service.LiveInfo;

import java.util.List;

/**
 * Created by zyh on 2017/4/19.
 */

public class AarCallback implements IMiLiveSdk.ICallback, IMiLiveSdk.IChannelCallback {
    private IMiLiveSdk.ICallback mCallback;
    private IMiLiveSdk.IChannelCallback mChannelCallback;

    public void setCallback(IMiLiveSdk.ICallback callback) {
        mCallback = callback;
    }

    public void setChannelCallback(IMiLiveSdk.IChannelCallback callback) {
        mChannelCallback = callback;

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
    public void notifyGetChannelLives(int errCode, List<LiveInfo> liveInfos) {
        if (mChannelCallback != null) {
            mChannelCallback.notifyGetChannelLives(errCode, liveInfos);
        }
    }
}
