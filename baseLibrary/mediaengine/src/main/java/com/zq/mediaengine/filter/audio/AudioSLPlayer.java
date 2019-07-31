package com.zq.mediaengine.filter.audio;

import android.util.Log;

import com.zq.mediaengine.util.LibraryLoader;

import java.nio.ByteBuffer;

/**
 * Audio pcm player with openSL ES.
 */

public class AudioSLPlayer implements IPcmPlayer {
    private static final String TAG = "AudioSLPlayer";

    private long mInstance;

    public AudioSLPlayer() {
        mInstance = _init();
    }

    public long getNativeInstance() {
        return mInstance;
    }

    public int config(int sampleFmt, int sampleRate, int channels, int bufferSamples, int fifoSizeInMs) {
        return _config(mInstance, sampleFmt, sampleRate, channels, bufferSamples, fifoSizeInMs);
    }

    public void attachTo(int idx, long ptr, boolean detach) {
        _attachTo(mInstance, idx, ptr, detach);
    }

    public void setTuneLatency(boolean tuneLatency) {
        _setTuneLatency(mInstance, tuneLatency);
    }

    public void setMute(boolean mute) {
        _setMute(mInstance, mute);
    }

    public int start() {
        return _start(mInstance);
    }

    public int stop() {
        return _stop(mInstance);
    }

    public int pause() {
        return _pause(mInstance);
    }

    public int resume() {
        return _resume(mInstance);
    }

    public int read(ByteBuffer buffer, int size) {
        int ret = _read(mInstance, buffer, size);
        if (ret >= 0) {
            buffer.rewind();
            buffer.limit(ret);
        }
        return ret;
    }

    public int write(ByteBuffer buffer) {
        return write(buffer, false);
    }

    @Override
    public int flush() {
        _stop(mInstance);
        _start(mInstance);
        return 0;
    }

    @Override
    public long getPosition() {
        return _getPosition(mInstance);
    }

    public int write(ByteBuffer buffer, boolean nonBlock) {
        return _write(mInstance, buffer, buffer.limit(), nonBlock);
    }

    public void release() {
        _release(mInstance);
    }

    private native long _init();
    private native int _config(long instance, int sampleFmt, int sampleRate, int channels,
                               int bufferSamples, int fifoSizeInMs);
    private native void _attachTo(long instance, int idx, long ptr, boolean detach);
    private native void _setTuneLatency(long instance, boolean tuneLatency);
    private native void _setMute(long instance, boolean mute);
    private native long _getPosition(long instance);
    private native int _start(long instance);
    private native int _stop(long instance);
    private native int _pause(long instance);
    private native int _resume(long instance);
    private native int _read(long instance, ByteBuffer buffer, int size);
    private native int _write(long instance, ByteBuffer buffer, int size, boolean nonBlock);
    private native void _release(long instance);

    static {
        LibraryLoader.load();
    }
}
