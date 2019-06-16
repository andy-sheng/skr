package com.engine;

import android.view.View;

public class UserStatus {
    private int userId;// 用户id
    private long enterTs; // 加入房间时间
    private boolean isVideoMute; // 视频是否暂时禁止
    private boolean isAudioMute;// 音频是否暂时禁止
    private boolean isFirstVideoDecoded; // 首帧视频是否decode
    private int mFirstVideoWidth;// 第一帧视频的宽高，一般就是视频流的宽高
    private int mFirstVideoHeight;
    private boolean isSelf = false;// 是否是本人，因为有可能用的引擎的账号系统
    private View mView; // 绑定的视图view
    private boolean enableVideo; // 视频是否可用
    private boolean isAnchor; // 是主播么

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

    public void setAudioMute(boolean audioMute) {
        isAudioMute = audioMute;
    }

    public boolean isAudioMute() {
        return isAudioMute;
    }

    public void setEnableVideo(boolean enableVideo) {
        this.enableVideo = enableVideo;
    }

    public boolean isEnableVideo() {
        return enableVideo;
    }

    public boolean isAnchor() {
        return isAnchor;
    }

    public void setAnchor(boolean anchor) {
        isAnchor = anchor;
    }

    public void setFirstVideoWidth(int firstVideoWidth) {
        mFirstVideoWidth = firstVideoWidth;
    }

    public int getFirstVideoWidth() {
        return mFirstVideoWidth;
    }

    public void setFirstVideoHeight(int firstVideoHeight) {
        mFirstVideoHeight = firstVideoHeight;
    }

    public int getFirstVideoHeight() {
        return mFirstVideoHeight;
    }

    @Override
    public String toString() {
        return "UserStatus{" +
                "userId=" + userId +
//                ", enterTs=" + enterTs +
                ", isVideoMute=" + isVideoMute +
                ", isAudioMute=" + isAudioMute +
                ", isFirstVideoDecoded=" + isFirstVideoDecoded +
                ", mFirstVideoWidth=" + mFirstVideoWidth +
                ", mFirstVideoHeight=" + mFirstVideoHeight +
//                ", isSelf=" + isSelf +
//                ", mView=" + mView +
                ", enableVideo=" + enableVideo +
//                ", isAnchor=" + isAnchor +
                '}';
    }
}
