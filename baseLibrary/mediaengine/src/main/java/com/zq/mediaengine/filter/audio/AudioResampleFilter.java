package com.zq.mediaengine.filter.audio;

import android.support.annotation.NonNull;

import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

import java.nio.ByteBuffer;

/**
 * Audio resample filter.
 * <p>
 * This module would be used to convert audio data from one format to another.
 */

public class AudioResampleFilter extends AudioFilterBase {
    private static final String TAG = "AudioResampleFilter";
    private AudioResample mAudioResample;
    private AudioBufFormat mOutFormat;
    private AudioBufFormat mUserOutFormat;
    private boolean mUseDiffMemory = false;

    public AudioResampleFilter() {
        mAudioResample = new AudioResample();
    }

    /**
     * Set audio output format.
     *
     * @param outFormat the dedicated output format
     */
    public void setOutFormat(@NonNull AudioBufFormat outFormat) {
        setOutFormat(outFormat, false);
    }

    public void setOutFormat(@NonNull AudioBufFormat outFormat, boolean useDiffMemory) {
        mUserOutFormat = outFormat;
        mUseDiffMemory = useDiffMemory;
    }

    @Override
    protected long getNativeInstance() {
        return mAudioResample.getNativeInstance();
    }

    private AudioBufFormat fixOutformat(AudioBufFormat inFormat, AudioBufFormat userFormat) {
        AudioBufFormat outFormat = new AudioBufFormat(userFormat);
        if (outFormat.sampleFormat < 0) {
            outFormat.sampleFormat = inFormat.sampleFormat;
        }
        if (outFormat.sampleRate < 0) {
            outFormat.sampleRate = inFormat.sampleRate;
        }
        if (outFormat.channels < 0) {
            outFormat.channels = inFormat.channels;
        }
        return outFormat;
    }

    @Override
    public AudioBufFormat getOutFormat(AudioBufFormat inFormat) {
        if(mUserOutFormat == null) {
            throw new IllegalArgumentException("you must call setOutFormat first");
        }
        mOutFormat = fixOutformat(inFormat, mUserOutFormat);
        mAudioResample.setOutputFormat(mOutFormat.sampleFormat, mOutFormat.sampleRate,
                mOutFormat.channels, mUseDiffMemory);
        return mOutFormat;
    }

    @Override
    protected void attachTo(int idx, long ptr, boolean detach) {
        mAudioResample.attachTo(idx, ptr, detach);
    }

    @Override
    protected int readNative(ByteBuffer buffer, int size) {
        return mAudioResample.read(buffer, size);
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        if(mUserOutFormat == null) {
            throw new IllegalArgumentException("you must call setOutFormat first");
        }
        mOutFormat = fixOutformat(format, mUserOutFormat);
        mAudioResample.setOutputFormat(mOutFormat.sampleFormat, mOutFormat.sampleRate,
                mOutFormat.channels, mUseDiffMemory);
        mAudioResample.config(format.sampleFormat, format.sampleRate, format.channels);
        return mOutFormat;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame inputFrame) {
        if (inputFrame.buf != null) {
            ByteBuffer outBuffer = mAudioResample.resample(inputFrame.buf);
            if (outBuffer != null) {
                return new AudioBufFrame(mOutFormat, outBuffer, inputFrame.pts);
            } else {
                return null;
            }
        } else {
            return inputFrame;
        }
    }

    @Override
    protected void doRelease() {
        if (mAudioResample != null) {
            mAudioResample.release();
            mAudioResample = null;
        }
    }
}
