package com.zq.mediaengine.encoder;

import android.util.Log;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.AudioEncodeConfig;
import com.zq.mediaengine.framework.AudioPacket;
import com.zq.mediaengine.util.FrameBufferCache;

import java.nio.ByteBuffer;

/**
 * Base class of audio encoder.
 */

public abstract class AudioEncoderBase extends Encoder<AudioBufFrame, AudioPacket> {
    private final static String TAG = "AudioEncoderBase";
    private final static boolean VERBOSE = false;

    protected static final int AUDIO_CACHE_NUM = 16;
    protected static final int AUDIO_CACHE_ITEM_SIZE = 8 * 1024;

    protected AudioEncodeConfig mAudioEncodeConfig;
    protected FrameBufferCache mAudioBufferCache;
    protected AudioEncodeConfig mOutConfig;

    public AudioEncoderBase() {
        mAudioBufferCache = new FrameBufferCache(AUDIO_CACHE_NUM, AUDIO_CACHE_ITEM_SIZE);
    }

    @Override
    public int getEncoderType() {
        return AVConst.MEDIA_TYPE_AUDIO;
    }

    /**
     * Start audio encoder in encoding thread.
     *
     * @param config Audio encode configuration.
     * @return 0 on success, or error code.
     */
    abstract protected int doStart(AudioEncodeConfig config);

    /**
     * Encode an audio frame.
     *
     * @param frame audio frame to be encoded.
     * @return 0 on success, or error code.
     */
    abstract protected int doEncode(AudioBufFrame frame);

    @Override
    protected int doStart(Object encodeConfig) {
        mAudioEncodeConfig = (AudioEncodeConfig) encodeConfig;
        mOutConfig = new AudioEncodeConfig(mAudioEncodeConfig);
        return doStart(mAudioEncodeConfig);
    }

    @Override
    protected boolean updateEncodeFormat(Object src, Object dst) {
        AudioBufFormat audioBufFormat = (AudioBufFormat) src;
        AudioEncodeConfig encodeConfig = (AudioEncodeConfig) dst;
        encodeConfig.sampleFmt = audioBufFormat.sampleFormat;
        encodeConfig.sampleRate = audioBufFormat.sampleRate;
        encodeConfig.channels = audioBufFormat.channels;
        return true;
    }

    @Override
    protected void doAdjustBitrate(int bitrate) {
        // do nothing
    }

    @Override
    protected boolean onFrameAvailable(AudioBufFrame frame) {
        if (frame == null || frame.buf == null) {
            return false;
        }

        ByteBuffer buffer = mAudioBufferCache.poll(frame.buf.limit());
        if (buffer == null) {
            Log.w(TAG, "Audio frame dropped, size=" + frame.buf.limit() + " pts=" + frame.pts);
            return true;
        }
        buffer.put(frame.buf);
        buffer.flip();
        frame.buf.rewind();
        frame.buf = buffer;
        return false;
    }

    @Override
    protected int doFrameAvailable(AudioBufFrame frame) {
        if (frame == null || frame.buf == null) {
            return 0;
        }
        if (VERBOSE) {
            Log.d(TAG, "audio frame in : size=" + frame.buf.limit() + " pts=" + frame.pts);
        }
        if (mMute) {
            for (int i = 0; i < frame.buf.limit(); i++) {
                frame.buf.put(i, (byte) 0);
            }
            frame.buf.rewind();
        }
        int ret = doEncode(frame);
        mAudioBufferCache.offer(frame.buf);
        return ret;
    }

    @Override
    protected void doFrameDropped(AudioBufFrame frame) {
        if (frame != null && frame.buf != null) {
            mAudioBufferCache.offer(frame.buf);
        }
    }

    protected void sendEncodedPacket(ByteBuffer data, long dts, long pts, int flags) {
        if (VERBOSE) {
            int size = (data != null) ? data.limit() : 0;
            Log.d(TAG, "encoded audio frame: size=" + size + " dts=" + dts + " pts=" + pts);
        }
        if ((flags & AVConst.FLAG_CODEC_CONFIG) != 0) {
            // trigger onFormatChanged event to next module
            mOutConfig = new AudioEncodeConfig(mAudioEncodeConfig);
            onEncodedFormatChanged(mOutConfig);
        }
        // send frame to next module
        AudioPacket packet = new AudioPacket(mOutConfig, data, pts);
        packet.flags = flags;
        onEncodedFrame(packet);
    }
}
