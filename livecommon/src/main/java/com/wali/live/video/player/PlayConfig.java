package com.wali.live.video.player;

/**
 * Created by chenyong on 16/8/9.
 */
public class PlayConfig {

    public static final int INTERRUPT_MODE_RELEASE_CREATE = 0;
    public static final int INTERRUPT_MODE_PAUSE_RESUME = 1;
    public static final int INTERRUPT_MODE_FINISH_OR_ERROR = 2;
    public static final int INTERRUPT_MODE_ESPORT_PAUSE_RESUME = 3;
    public boolean isStream = false;
    public int interruptMode = INTERRUPT_MODE_RELEASE_CREATE;

    public static final int SHORT_VIDEO_MODE = 3;
    public static final int LIVE_VIDEO_MODE = 4;
    public static final int OTHER_MODE = 5;
    public int videoMode = INTERRUPT_MODE_RELEASE_CREATE;

    public void setVideoMode(int videoMode) {
        this.videoMode = videoMode;
    }

    public int getVideoMode() {
        return videoMode;
    }

    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean isStream) {
        this.isStream = isStream;
    }

    public int getInterruptMode() {
        return interruptMode;
    }

    public void setInterruptMode(int interruptMode) {
        this.interruptMode = interruptMode;
    }

    private static class SingletonHolder {
        private static final PlayConfig INSTANCE = new PlayConfig();
    }

    private PlayConfig() {
    }

    public static PlayConfig getInstance() {
        return SingletonHolder.INSTANCE;
    }

}
