package com.zq.mediaengine.filter.audio;

import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.SinkPin;

import java.nio.ByteBuffer;

/**
 * APM filter.
 * <p>
 * This module would be used to process audio data to achieve NoiseSuppression, GainControl and other functions
 */

public class APMFilter extends AudioFilterBase {
    private static final String TAG = "APMFilter";

    public final static int NS_LEVEL_0 = APMWrapper.NS_LEVEL_LOW;
    public final static int NS_LEVEL_1 = APMWrapper.NS_LEVEL_MODERATE;
    public final static int NS_LEVEL_2 = APMWrapper.NS_LEVEL_HIGH;
    public final static int NS_LEVEL_3 = APMWrapper.NS_LEVEL_VERYHIGH;

    public final static int AEC_ROUTING_MODE_HEADSET = APMWrapper.AEC_ROUTING_MODE_HEADSET;
    public final static int AEC_ROUTING_MODE_EARPIECE = APMWrapper.AEC_ROUTING_MODE_EARPIECE;
    public final static int AEC_ROUTING_MODE_LOUD_EARPIECE = APMWrapper.AEC_ROUTING_MODE_LOUD_EARPIECE;
    public final static int AEC_ROUTING_MODE_SPEAKER_PHONE = APMWrapper.AEC_ROUTING_MODE_SPEAKER_PHONE;
    public final static int AEC_ROUTING_MODE_LOUD_SPEAKER_PHONE = APMWrapper.AEC_ROUTING_MODE_LOUD_SPEAKER_PHONE;

    private APMWrapper mApmWrapper;

    public APMFilter() {
        mApmWrapper = new APMWrapper();
    }

    public SinkPin<AudioBufFrame> getReverseSinkPin() {
        return mReverseSinkPin;
    }

    public int enableNs(boolean enable) {
        return mApmWrapper.enableNs(enable);
    }

    public int setNsLevel(int level) {
        return mApmWrapper.setNsLevel(level);
    }

    public int enableAECM(boolean enable) {
        return mApmWrapper.enableAECM(enable);
    }

    public int enableAEC(boolean enable) {
        return mApmWrapper.enableAEC(enable);
    }

    public int setRoutingMode(int mode) {
        return mApmWrapper.setRoutingMode(mode);
    }

    public int setStreamDelay(int delay) {
        return mApmWrapper.setStreamDelay(delay);
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        if (format == null) {
            return null;
        }
        mApmWrapper.config(0, format.sampleFormat, format.sampleRate,format.channels);
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        ByteBuffer outBuffer = mApmWrapper.processStream(0, frame.buf);
        if (outBuffer == null){
            return frame;
        }
        return new AudioBufFrame(mApmWrapper.getAPMFormat(), outBuffer, frame.pts, frame.flags);
    }

    @Override
    protected void doRelease() {
        if (mApmWrapper != null) {
            mApmWrapper.release();
            mApmWrapper = null;
        }
    }

    @Override
    protected void attachTo(int idx, long ptr, boolean detach) {
        mApmWrapper.attachTo(idx, ptr, detach);
    }

    @Override
    protected int readNative(ByteBuffer buffer, int size) {
        return mApmWrapper.read(buffer, size);
    }

    @Override
    protected long getNativeInstance() {
        return mApmWrapper.getNativeInstance();
    }

    private SinkPin<AudioBufFrame> mReverseSinkPin = new SinkPin<AudioBufFrame>() {
        @Override
        public void onFormatChanged(Object format) {
            AudioBufFormat audioBufFormat = (AudioBufFormat) format;
            mApmWrapper.config(1, audioBufFormat.sampleFormat, audioBufFormat.sampleRate, audioBufFormat.channels);
        }

        @Override
        public void onFrameAvailable(AudioBufFrame frame) {
            if (mApmWrapper != null) {
                mApmWrapper.processStream(1, frame.buf);
            }
        }
    };
}
