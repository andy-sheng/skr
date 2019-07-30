package com.common.player;

import android.media.MediaPlayer;

public class PlayerCallbackAdapter implements IPlayerCallback {

    @Override
    public void onPrepared() {

    }

    @Override
    public void onCompletion() {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onVideoSizeChanged(int width, int height) {

    }

    @Override
    public void onError(int what, int extra) {

    }

    @Override
    public void onInfo(int what, int extra) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean openTimeFlyMonitor() {
        return false;
    }

    @Override
    public void onTimeFlyMonitor(long pos, long duration) {

    }
}
