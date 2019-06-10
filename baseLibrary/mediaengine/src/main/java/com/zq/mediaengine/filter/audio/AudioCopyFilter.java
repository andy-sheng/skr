package com.zq.mediaengine.filter.audio;

import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Audio copy filter.
 */

public class AudioCopyFilter extends AudioFilterBase {
    private static final String TAG = "AudioCopyFilter";

    private AudioBufFormat mOutFormat;
    private ByteBuffer mOutBuffer;

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        mOutFormat = new AudioBufFormat(format);
        mOutFormat.nativeModule = 0;
        return mOutFormat;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        if (mOutBuffer == null || mOutBuffer.capacity() < frame.buf.limit()) {
            mOutBuffer = ByteBuffer.allocateDirect(frame.buf.limit());
            mOutBuffer.order(ByteOrder.nativeOrder());
        }
        mOutBuffer.clear();
        mOutBuffer.put(frame.buf);
        frame.buf.rewind();
        mOutBuffer.flip();

        AudioBufFrame outFrame = new AudioBufFrame(frame);
        frame.format = mOutFormat;
        frame.buf = mOutBuffer;
        return outFrame;
    }
}
