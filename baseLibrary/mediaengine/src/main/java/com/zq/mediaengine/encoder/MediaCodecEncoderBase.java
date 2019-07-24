package com.zq.mediaengine.encoder;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.zq.mediaengine.framework.AVFrameBase;
import com.zq.mediaengine.framework.AVPacketBase;

import java.nio.ByteBuffer;

/**
 * MediaCodec encoder module base.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
abstract public class MediaCodecEncoderBase<I extends AVFrameBase, O extends AVPacketBase>
        extends Encoder<I,O> {
    private final static String TAG = "MediaCodecEncoderBase";
    private final static boolean VERBOSE = false;
    private static final long TIMEOUT_USEC = 10000;
    private static final int MAX_EOS_SPINS = 10;

    protected MediaCodec mEncoder;
    protected MediaCodec.BufferInfo mBufferInfo;
    protected boolean mForceEos = false;
    private int mEosSpinCount = 0;

    /**
     * This method should be called before the last input packet is queued
     * Some devices don't honor MediaCodec#signalEndOfInputStream
     * e.g: Google Glass
     */
    public void signalEndOfStream() {
        mForceEos = true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void doAdjustBitrate(int targetBitrate) {
        if (mEncoder == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Bundle bitrate = new Bundle();
            bitrate.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, targetBitrate);
            mEncoder.setParameters(bitrate);
        } else {
            Log.w(TAG, "Ignoring adjustVideoBitrate call. " +
                    "This functionality is only available on Android API 19+");
        }
    }

    // null buffer means EOS
    protected void fillEncoder(ByteBuffer buffer, long ptsUs) {
        if (VERBOSE) Log.d(TAG, "fillEncoder(" + buffer + ", " + ptsUs/1000 + ")");
        ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
        int index = mEncoder.dequeueInputBuffer(0);

        if (index >= 0) {
            ByteBuffer buf = inputBuffers[index];
            buf.clear();
            if (buffer != null) {
                buf.put(buffer);
                buffer.rewind();
                mEncoder.queueInputBuffer(index, 0, buffer.limit(), ptsUs, 0);
            } else {
                int flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                mEncoder.queueInputBuffer(index, 0, 0, ptsUs, flags);
            }
        }
    }

    protected void drainEncoder(boolean endOfStream) {
        drainEncoder(0, endOfStream);
    }

    protected void drainEncoder(long timeout, boolean endOfStream) {
        if (VERBOSE) Log.d(TAG, "drainEncoder(" + timeout + ", " + endOfStream + ")");

        if (mBufferInfo == null) {
            mBufferInfo = new MediaCodec.BufferInfo();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus;
            try {
                encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, timeout);
            } catch (Exception e) {
                // Exynos socs may report invalid state exception even on a valid state,
                // when no input frame filled but eos signaled.
                if (VERBOSE) Log.e(TAG, "dequeueOutputBuffer failed");
                break;
            }
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    timeout = TIMEOUT_USEC;
                    mEosSpinCount++;
                    if (mEosSpinCount > MAX_EOS_SPINS) {
                        if (VERBOSE) {
                            Log.i(TAG, "Force shutting down Muxer for MAX_EOS_SPINS reached");
                        }
                        mBufferInfo.flags |= MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                        O frame = getOutFrame(null, mBufferInfo);
                        onEncodedFrame(frame);
                        break;
                    }
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                MediaFormat newFormat = mEncoder.getOutputFormat();
                if (VERBOSE) Log.d(TAG, "encoder output format changed: " + newFormat);
                // CONFIG_FRAME will be got right after this, now we ignore this
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                // Allow zero length buffer for purpose of sending 0 size video EOS Flag
                if (mBufferInfo.size >= 0) {
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    if (mForceEos) {
                        mBufferInfo.flags = mBufferInfo.flags |
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                        Log.i(TAG, "Forcing EOS");
                    }

                    O frame = getOutFrame(encodedData, mBufferInfo);
                    onEncodedFrame(frame);
                    if (VERBOSE) {
                        Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, \t ts=" +
                                mBufferInfo.presentationTimeUs);
                    }
                    mEncoder.releaseOutputBuffer(encoderStatus, false);
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE)
                            Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
        if (endOfStream) {
            if (VERBOSE) {
                Log.i(TAG, "final data drain complete");
            }
        }
    }

    abstract protected O getOutFrame(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo);
}
