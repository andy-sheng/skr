package com.zq.mediaengine.filter.audio;

import android.util.Log;

import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * Calculate audio signal level.
 */
public class AudioLevelMeterFilter extends AudioFilterBase {
    private static final String TAG = "AudioLevelMeterFilter";
    private static final boolean VERBOSE = false;

    private static final int MAX_NUM = 8;

    private AudioBufFormat mAudioBufFormat;
    private String mTag = "";
    private boolean mNeedCal;
    private float[] mAmplitudes;
    private boolean[] mEnabled;
    private boolean[] mNeedFlush;

    public AudioLevelMeterFilter() {
        mAmplitudes = new float[MAX_NUM];
        mEnabled = new boolean[MAX_NUM];
        mNeedFlush = new boolean[MAX_NUM];
        mNeedCal = false;

        for(int i = 0; i < MAX_NUM; i++) {
            mAmplitudes[i] = 0.f;
            mEnabled[i] = false;
            mNeedFlush[i] = false;
        }
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public void setEnableLevelMeter(int idx, boolean enable) {
        if (idx < 0 || idx >= MAX_NUM) {
            return;
        }

        mEnabled[idx] = enable;
        if (!enable) {
            mNeedFlush[idx] = true;
            boolean disabled = true;
            for (int i = 0; i < MAX_NUM; i++) {
                if (mEnabled[i]) {
                    disabled = false;
                    break;
                }
            }
            if (disabled) {
                mNeedCal = false;
            }
        } else {
            mNeedCal = true;
        }
    }

    public void updateMeter(int idx) {
        if (idx < 0 || idx >= MAX_NUM) {
            return;
        }
        mNeedFlush[idx] = true;
    }

    /**
     * Get maximum volume between every getMaxVolume call.
     *
     * @param idx index
     * @return audio amplitude in [0-1]
     */
    public float getMaxAmplitude(int idx) {
        if (idx < 0 || idx >= MAX_NUM) {
            return 0.f;
        }
        return mAmplitudes[idx];
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        mAudioBufFormat = format;
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        if (frame != null && frame.buf != null && mNeedCal) {
            doLevelMeter(frame.buf);
        }
        return frame;
    }

    private void doLevelMeter(ByteBuffer buffer) {
        int maxVal = 0;
        ShortBuffer shortBuffer = buffer.asShortBuffer();
        for (int i = 0; i < shortBuffer.limit(); i++) {
            maxVal = Math.max(Math.abs(shortBuffer.get(i)), maxVal);
        }
        shortBuffer.rewind();

        float maxValF = Math.abs(maxVal) / 32768.f;
        if (VERBOSE) {
            Log.d(TAG, mTag + " maxVal: " + maxVal + " " + maxValF);
        }
        for (int i = 0; i < MAX_NUM; i++) {
            if (mEnabled[i]) {
                if (mNeedFlush[i]) {
                    mAmplitudes[i] = 0;
                    mNeedFlush[i] = false;
                }
                mAmplitudes[i] = Math.max(mAmplitudes[i], maxValF);
                if (VERBOSE) {
                    Log.d(TAG, mTag + " mAmplitudes[" + i + "]: " + mAmplitudes[i]);
                }
            }
        }
    }
}
