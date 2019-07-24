package com.component.mediaengine.filter.audio;

import android.support.annotation.NonNull;

import com.component.mediaengine.framework.AudioBufFormat;
import com.component.mediaengine.framework.AudioBufFrame;

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

    public AudioResampleFilter() {
        mAudioResample = new AudioResample();
    }

    /**
     * Set audio output format.
     *
     * @param outFormat the dedicated output format
     */
    public void setOutFormat(@NonNull AudioBufFormat outFormat) {
        mOutFormat = outFormat;
        mAudioResample.setOutputFormat(outFormat.sampleFormat, outFormat.sampleRate, outFormat.channels);
    }

    public void setOutFormat(@NonNull AudioBufFormat outFormat, boolean useDiffMemory) {
        mOutFormat = outFormat;
        mAudioResample.setOutputFormat(outFormat.sampleFormat, outFormat.sampleRate,
                outFormat.channels, useDiffMemory);
    }

    @Override
    protected long getNativeInstance() {
        return mAudioResample.getNativeInstance();
    }

    @Override
    public AudioBufFormat getOutFormat() {
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
        if(mOutFormat == null) {
            throw new IllegalArgumentException("you must call setOutFormat first");
        }
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
