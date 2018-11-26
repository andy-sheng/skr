package com.engine;

public class UserStatus {
    private String userId;
    private long enterTs;
    private boolean isVideoMute;
    private boolean isFirstVideoDecoded;

    public UserStatus(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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
}
