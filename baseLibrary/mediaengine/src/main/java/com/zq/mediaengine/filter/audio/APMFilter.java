package com.zq.mediaengine.filter.audio;

import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

import java.nio.ByteBuffer;

/**
 * APM filter.
 * <p>
 * This module would be used to process audio data to achieve NoiseSuppression, GainControl and other functions
 */

public class APMFilter extends AudioFilterBase {
    private APMWrapper mApmWrapper;

    public APMFilter() {
        mApmWrapper = new APMWrapper();
    }

    public int enableNs(boolean enable) {
        return mApmWrapper.enableNs(enable);
    }

    public int setNsLevel(int level) {
        return mApmWrapper.setNsLevel(level);
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        if (format == null) {
            return null;
        }
        mApmWrapper.config(format.sampleRate,format.channels);
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        ByteBuffer outBuffer = mApmWrapper.processStream(frame.buf);
        if (outBuffer == null){
            return frame;
        }
        return new AudioBufFrame(mApmWrapper.getAPMFormate(), outBuffer, frame.pts);
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

}
