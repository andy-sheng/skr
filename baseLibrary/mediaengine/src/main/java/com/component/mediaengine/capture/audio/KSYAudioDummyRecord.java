package com.component.mediaengine.capture.audio;

import java.nio.ByteBuffer;

/**
 * Dummy audio record.
 *
 * @hide
 */

public class KSYAudioDummyRecord implements IKSYAudioRecord {

    private int mSampleRate;
    private int mFrameSize;
    private int mAtomSleepMs;

    private long mLastTime;
    private int mFifoBytes;

    public KSYAudioDummyRecord(int sampleRate, int channels, int bufferSamples) {
        mSampleRate = sampleRate;
        mFrameSize = channels * 2;
        mAtomSleepMs = bufferSamples * 1000 / sampleRate;
        mFifoBytes = 0;
    }

    @Override
    public void setVolume(float volume) {
        // do nothing
    }

    @Override
    public int startRecording() {
        mFifoBytes = 0;
        mLastTime = System.nanoTime() / 1000;
        return 0;
    }

    @Override
    public int stop() {
        return 0;
    }

    @Override
    public void release() {

    }

    @Override
    public int read(ByteBuffer buffer, int size) {
        if (buffer == null || buffer.capacity() < size) {
            return 0;
        }

        while (mFifoBytes < size) {
            try {
                Thread.sleep(mAtomSleepMs);
            } catch (InterruptedException e) {
                return 0;
            }
            long now = System.nanoTime() / 1000;
            mFifoBytes += timeToBytes(now - mLastTime);
            mLastTime = now;
        }
        mFifoBytes -= size;

        for (int i = 0; i < size; i++) {
            buffer.put(i, (byte) 0);
        }
        buffer.limit(size);
        buffer.rewind();
        return size;
    }

    @Override
    public long getNativeModule() {
        return 0;
    }

    @Override
    public void setEnableLatencyTest(boolean enable) {
    }

    private int timeToBytes(long us) {
        return (int) (mSampleRate * us / 1000000 * mFrameSize);
    }
}
