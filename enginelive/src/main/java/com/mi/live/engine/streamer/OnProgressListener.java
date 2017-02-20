package com.mi.live.engine.streamer;

/**
 * Created by simon on 16-3-6.
 */
public interface OnProgressListener {
    void onMusicStarted();
    void onMusicResumed();
    void onMusicProgress(long currTime);
    void onMusicPaused();
    void onMusicCompleted();
    void onMusicStopped();
}
