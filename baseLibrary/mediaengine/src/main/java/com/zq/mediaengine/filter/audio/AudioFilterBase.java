package com.zq.mediaengine.filter.audio;

import android.util.Log;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Base class of audio filters. With an input pin and an output pin.
 */

public abstract class AudioFilterBase {
    private static final String TAG = "AudioFilterBase";

    /**
     * Input pin
     */
    private SinkPin<AudioBufFrame> mSinkPin;

    /**
     * Output pin
     */
    private SrcPin<AudioBufFrame> mSrcPin;

    private AudioBufFormat mInFormat;
    private AudioBufFormat mOutFormat;
    private ByteBuffer mByteBuffer;

    // for native read, especially for resample
    private long mTotalInputSize;
    private long mTotalReadSize;

    public AudioFilterBase() {
        mSinkPin = new AudioFilterSinkPin();
        mSrcPin = new AudioBufSrcPin();
    }

    /**
     * Get input pin instance.
     *
     * @return input pin instance.
     */
    public SinkPin<AudioBufFrame> getSinkPin() {
        return mSinkPin;
    }

    /**
     * Get output pin instance.
     *
     * @return output pin instance.
     */
    public SrcPin<AudioBufFrame> getSrcPin() {
        return mSrcPin;
    }

    /**
     * Get native instance of this filter.
     *
     * @return native instance ptr or 0
     */
    protected long getNativeInstance() {
        return 0;
    }

    /**
     * Read filtered data from native side.
     *
     * @param buffer buffer to store data
     * @param size   data size to read in bytes
     * @return read size
     */
    protected int readNative(ByteBuffer buffer, int size) {
        return 0;
    }

    /**
     * Attach to previous filter
     *
     * @param idx index defined by this filter
     * @param ptr previous filter native ptr
     * @param detach is detach
     */
    protected void attachTo(int idx, long ptr, boolean detach) {
    }

    /**
     * Get output format of current filter, should be implemented by child class.
     *
     * @return audio output format
     */
    protected AudioBufFormat getOutFormat(AudioBufFormat inFormat) {
        return null;
    }

    /**
     * Handle format changed event by inherit this method.
     *
     * @param format the new audio format
     * @return the output audio format
     */
    abstract protected AudioBufFormat doFormatChanged(AudioBufFormat format);

    /**
     * Do actual filter.
     *
     * @param frame input audio frame
     * @return output audio frame
     */
    abstract protected AudioBufFrame doFilter(AudioBufFrame frame);

    /**
     * Release resources needed to be released.
     */
    protected void doRelease() {}

    public void release() {
        mSrcPin.disconnect(true);
        doRelease();
    }

    private class AudioFilterSinkPin extends SinkPin<AudioBufFrame> {

        @Override
        public void onFormatChanged(Object format) {
            if (format == null) {
                return;
            }

            mInFormat = (AudioBufFormat) format;
            Log.d(TAG, "doFormatChanged nativeModule=" + mInFormat.nativeModule);

            if (mInFormat.nativeModule != 0 && getNativeInstance() != 0) {
                // fill data by native
                attachTo(0, mInFormat.nativeModule, false);
                AudioBufFormat outFormat = getOutFormat(mInFormat);
                if (outFormat == null) {
                    mOutFormat = new AudioBufFormat(mInFormat);
                } else {
                    mOutFormat = new AudioBufFormat(outFormat);
                }
                mOutFormat.nativeModule = getNativeInstance();

                // init native read
                mTotalInputSize = 0;
                mTotalReadSize = 0;
            } else {
                mOutFormat = doFormatChanged(mInFormat);
                if (mOutFormat == null) {
                    return;
                }
                if (mOutFormat == mInFormat) {
                    mOutFormat = new AudioBufFormat(mInFormat);
                    mOutFormat.nativeModule = 0;
                }
            }
            mSrcPin.onFormatChanged(mOutFormat);
        }

        @Override
        public void onFrameAvailable(AudioBufFrame frame) {
            if (frame == null || frame.format == null) {
                return;
            }

            AudioBufFrame outFrame = frame;
            if (frame.format.nativeModule != 0 && getNativeInstance() != 0) {
                if (frame.buf != null) {
                    AudioBufFormat format = frame.format;
                    mTotalInputSize += frame.buf.limit();
                    long totalNeedRead = (mTotalInputSize / (format.channels * 2)) *
                            mOutFormat.sampleRate / format.sampleRate * (mOutFormat.channels * 2);
                    int size = (int) (totalNeedRead - mTotalReadSize);
                    mTotalReadSize += size;
                    ByteBuffer byteBuffer = frame.buf;
                    if (byteBuffer.capacity() < size) {
                        if (mByteBuffer == null || mByteBuffer.capacity() < size) {
                            mByteBuffer = ByteBuffer.allocateDirect(size);
                            mByteBuffer.order(ByteOrder.nativeOrder());
                        }
                        byteBuffer = mByteBuffer;
                    }
                    int ret = readNative(byteBuffer, size);
                    if (ret <= 0) {
                        Log.e(TAG, AudioFilterBase.this.getClass().getSimpleName() +
                                " readNative failed ret=" + ret);
                    }
                    outFrame = new AudioBufFrame(frame);
                    outFrame.format = mOutFormat;
                    outFrame.buf = byteBuffer;
                }

                // detach filter after eos got
                if ((frame.flags & AVConst.FLAG_DETACH_NATIVE_MODULE) != 0) {
                    attachTo(0, frame.format.nativeModule, true);
                }
            } else if (frame.buf != null) {
                if (!frame.buf.isDirect() && !frame.buf.hasArray()) {
                    Log.e(TAG, "input frame must be direct ByteBuffer or array backed ByteBuffer");
                }
                outFrame = doFilter(frame);
            }

            if (outFrame == null) {
                return;
            }
            if (outFrame == frame) {
                outFrame = new AudioBufFrame(frame);
                outFrame.format = mOutFormat;
            }
            mSrcPin.onFrameAvailable(outFrame);
        }

        @Override
        public void onDisconnect(boolean recursive) {
            if (recursive) {
                release();
            }
        }
    }
}
