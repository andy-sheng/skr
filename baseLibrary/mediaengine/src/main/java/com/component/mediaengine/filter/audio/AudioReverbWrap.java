package com.component.mediaengine.filter.audio;

import com.component.mediaengine.util.LibraryLoader;

import java.nio.ByteBuffer;

/**
 * @hide
 */

public class AudioReverbWrap {
    private long mReverbInstance;

    public AudioReverbWrap() {
        mReverbInstance = create();
    }

    public long getNativeInstance() {
        return mReverbInstance;
    }

    public void config(int sampleFmt, int sampleRate, int channels) {
        config(mReverbInstance, sampleFmt, sampleRate, channels);
    }

    public void attachTo(int idx, long ptr, boolean detach) {
        attachTo(mReverbInstance, idx, ptr, detach);
    }

    public void setReverbLevel(int level) {
        setLevel(mReverbInstance, level);
    }

    public int read(ByteBuffer byteBuffer, int size) {
        return read(mReverbInstance, byteBuffer, size);
    }

    public int processReverb(ByteBuffer buf) {
        if (buf != null && buf.limit() > 0) {
            return process(mReverbInstance, buf, buf.limit());
        } else {
            return 0;
        }
    }

    public void release() {
        delete(mReverbInstance);
    }

    private native long create();
    private native void config(long reverbInstance, int sampleFmt, int sampleRate, int channels);
    private native void attachTo(long reverbInstance, int idx, long ptr, boolean detach);
    private native boolean setLevel(long reverbInstance, int level);
    private native int read(long reverbInstance, ByteBuffer buf, int size);
    private native int process(long reverbInstance, ByteBuffer buf, int size);
    private native boolean delete(long reverbInstance);

    static {
        LibraryLoader.load();
    }
}
