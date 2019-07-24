package com.component.mediaengine.filter.audio;

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

    int start();

    int stop();

    int pause();

    int resume();

    int write(ByteBuffer buffer);

    int flush();

    long getPosition();

    void release();
}
