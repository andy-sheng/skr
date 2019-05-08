package com.zq.mediaengine.filter.audio;

import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.util.LibraryLoader;

import java.nio.ByteBuffer;

/**
 * Audio resample implemented with swr.
 */

public class AudioResample {
    private long mInstance;

    public AudioResample() {
        mInstance = _init();
    }

    public AudioResample(AudioBufFormat inFormat, AudioBufFormat outFormat) {
        mInstance = _init();
        setOutputFormat(outFormat.sampleRate, outFormat.channels);
        config(inFormat.sampleFormat, inFormat.sampleRate, inFormat.channels);
    }

    public long getNativeInstance() {
        return mInstance;
    }

    /**
     * Set output audio format, must set before {@link #config(int, int, int)}.
     *
     * @param sampleRate output sample rate
     * @param channels   output channel number
     */
    public void setOutputFormat(int sampleRate, int channels) {
        _setOutputFormat(mInstance, sampleRate, channels);
    }

    /**
     * Set input audio format, and start resample.
     *
     * @param sampleFormat input sample format
     * @param sampleRate input sample rate
     * @param channels   input channel number
     * @return 0 if success, others if failed.
     */
    public int config(int sampleFormat, int sampleRate, int channels) {
        return _config(mInstance, sampleFormat, sampleRate, channels);
    }

    public void attachTo(int idx, long ptr, boolean detach) {
        _attachTo(mInstance, idx, ptr, detach);
    }

    public int read(ByteBuffer buffer, int size) {
        int ret =  _read(mInstance, buffer, size);
        if (ret >= 0) {
            buffer.rewind();
            buffer.limit(ret);
        }
        return ret;
    }

    public ByteBuffer resample(ByteBuffer buffer) {
        if (buffer == null || buffer.limit() == 0) {
            return null;
        }
        return _resample(mInstance, buffer, buffer.limit());
    }

    public void release() {
        _release(mInstance);
    }

    private native long _init();
    private native void _setOutputFormat(long instance, int sampleRate, int channels);
    private native int _config(long instance, int sampleFormat, int sampleRate, int channels);
    private native void _attachTo(long instance, int idx, long ptr, boolean detach);
    private native int _read(long instance, ByteBuffer buffer, int size);
    private native ByteBuffer _resample(long instance, ByteBuffer buffer, int size);
    private native void _release(long instance);

    static {
        LibraryLoader.load();
    }
}
