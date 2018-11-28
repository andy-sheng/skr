package com.engine;

import android.view.View;

public class UserStatus {
    private int userId;
    private long enterTs;
    private boolean isVideoMute;
    private boolean isFirstVideoDecoded;
    private boolean isSelf = false;// 是否是本人，因为有可能用的引擎的账号系统
    private View mView;

    public UserStatus(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getEnterTs() {
        return enterTs;
    }

    public void setEnterTs(long enterTs) {
        this.enterTs = enterTs;
    }

    public boolean isVideoMute() {
        return isVideoMute;
    }

    public void setVideoMute(boolean videoMute) {
        isVideoMute = videoMute;
    }

    public boolean isFirstVideoDecoded() {
        return isFirstVideoDecoded;
    }

    public void setFirstVideoDecoded(boolean firstVideoDecoded) {
        isFirstVideoDecoded = firstVideoDecoded;
    }

    public void setIsSelf(boolean self) {
        isSelf = self;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        mView = view;
    }

    public boolean hasBindView() {
        return mView != null;
    }
}
