package com.zq.mediaengine.capture.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * Audio recorder with AudioRecord.
 *
 * @hide
 */

public class UnionAudioRecord implements IUnionAudioRecord {
    private static final String TAG = "UnionAudioRecord";

    private AudioRecord mAudioRecord;

    private float mVolume = 1.0f;
    private boolean mLatencyTest;
    private long mStartTime;

    /**
     * Construct UnionAudioRecord
     *
     * @param sampleRate sample rate in HZ
     * @param channels channel number, only 1 and 2 valid
     * @param bufferSamples atom buffer samples
     */
    public UnionAudioRecord(int sampleRate, int channels, int bufferSamples) {
        int channelConfig = (channels == 1) ? AudioFormat.CHANNEL_IN_MONO :
                AudioFormat.CHANNEL_IN_STEREO;
        int bufferSize = bufferSamples * channels * 2;
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    }

    @Override
    public void setVolume(float volume) {
        mVolume = volume;
    }

    @Override
    public int startRecording() {
        try {
            mAudioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    @Override
    public int stop() {
        try {
            mAudioRecord.stop();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    @Override
    public void release() {
        mAudioRecord.release();
    }

    @Override
    public int read(ByteBuffer buffer, int size) {
        int ret = mAudioRecord.read(buffer, size);
        if (ret <= 0) {
            return ret;
        }

        // rewind buffer
        buffer.limit(ret);
        buffer.rewind();

        if (mLatencyTest) {
            long now = System.nanoTime() / 1000;
            ShortBuffer shortBuffer = buffer.asShortBuffer();

            // measure latency
            short threshold = Short.MAX_VALUE / 4;
            for (int i = 0; i < shortBuffer.limit(); i++) {
                if (shortBuffer.get(i) >= threshold) {
                    int latency = (int) ((now - mStartTime) / 1000);
                    Log.i(TAG, "Latency measured : " + latency + " ms");
                    break;
                }
            }

            // clear buffer
            for (int i = 0; i < shortBuffer.limit(); i++) {
                shortBuffer.put(i, (short) 0);
            }

            // trigger impulse
            if ((now - mStartTime) >= 5000000L) {
                mStartTime = now;
                shortBuffer.put(0, Short.MAX_VALUE);
            }
            shortBuffer.rewind();
        } else if (mVolume != 1.0f) {
            ShortBuffer shortBuffer = buffer.asShortBuffer();
            for (int i = 0; i < shortBuffer.limit(); i++) {
                shortBuffer.put(i, clipShort((int) (shortBuffer.get(i) * mVolume)));
            }
            shortBuffer.rewind();
        }
        return ret;
    }

    @Override
    public long getNativeModule() {
        return 0;
    }

    @Override
    public void setEnableLatencyTest(boolean enable) {
        mLatencyTest = enable;
    }

    private short clipShort(int a) {
        if (((a+0x8000) & ~0xFFFF) != 0)
            return (short) ((a >> 31) ^ 0x7FFF);
        else
            return (short) a;
    }
}
