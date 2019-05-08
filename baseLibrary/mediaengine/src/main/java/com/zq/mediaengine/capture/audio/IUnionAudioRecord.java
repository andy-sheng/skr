package com.zq.mediaengine.capture.audio;

import java.nio.ByteBuffer;

/**
 * Base class of audio record.
 */

public interface IUnionAudioRecord {

    void setVolume(float volume);

    int startRecording();

    int stop();

    void release();

    int read(ByteBuffer buffer, int size);

    long getNativeModule();

    void setEnableLatencyTest(boolean enable);
}
