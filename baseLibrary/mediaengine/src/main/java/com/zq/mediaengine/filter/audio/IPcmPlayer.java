package com.zq.mediaengine.filter.audio;

import java.nio.ByteBuffer;

/**
 * PCM player interface.
 *
 * @hide
 */

public interface IPcmPlayer {

    long getNativeInstance();

    int config(int sampleFmt, int sampleRate, int channels, int bufferSamples, int fifoSizeInMs);

    void setMute(boolean mute);

    void setVolume(float volume);

    int start();

    int stop();

    int pause();

    int resume();

    void attachTo(int idx, long ptr, boolean detach);

    int read(ByteBuffer buffer, int size);

    int write(ByteBuffer buffer);

    int write(ByteBuffer buffer, boolean nonBlock);

    int flush();

    long getPosition();

    void release();
}
