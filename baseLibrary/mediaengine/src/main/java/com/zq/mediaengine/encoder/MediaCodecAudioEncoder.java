package com.zq.mediaengine.encoder;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.AudioCodecFormat;
import com.zq.mediaengine.framework.AudioPacket;
import com.zq.mediaengine.util.FrameBufferCache;

import java.nio.ByteBuffer;

/**
 * MediaCodec Audio encoder.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MediaCodecAudioEncoder extends MediaCodecEncoderBase<AudioBufFrame, AudioPacket> {
    private static final String TAG = "HWAudioEncoder";

    private static final int AUDIO_CACHE_NUM = 16;
    private static final int AUDIO_CACHE_ITEM_SIZE = 8 * 1024;

    private AudioCodecFormat mOutFormat;
    private FrameBufferCache mAudioBufferCache;

    public MediaCodecAudioEncoder() {
        mAudioBufferCache = new FrameBufferCache(AUDIO_CACHE_NUM, AUDIO_CACHE_ITEM_SIZE);
    }

    @Override
    protected int doStart(Object encodeFormat) {
        AudioCodecFormat format = (AudioCodecFormat) encodeFormat;
        MediaFormat mediaFormat = format.toMediaFormat();
        try {
            mEncoder = MediaCodec.createEncoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
            mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncoder.start();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start MediaCodec audio encoder");
            e.printStackTrace();
            return ENCODER_ERROR_UNSUPPORTED;
        }

        // trigger format changed event
        mOutFormat = new AudioCodecFormat((AudioCodecFormat) mEncodeFormat);
        onEncodedFormatChanged(mOutFormat);

        // try to get codec config frame
        drainEncoder(20 * 1000, false);

        return 0;
    }

    @Override
    protected void doStop() {
        try {
            // signal eos
            fillEncoder(null, 0);
            drainEncoder(true);
        } catch (Exception e) {
            // ignore
        }
        try {
            mEncoder.stop();
        } catch (Exception e) {
            Log.e(TAG, "stop encoder failed, ignore");
        }
        mEncoder.release();
        mEncoder = null;
        Log.i(TAG, "MediaCodec released");

        AudioPacket packet = new AudioPacket(mOutFormat, null, 0);
        packet.flags |= AVConst.FLAG_END_OF_STREAM;
        onEncodedFrame(packet);
    }

    @Override
    protected boolean updateEncodeFormat(Object src, Object dst) {
        AudioBufFormat audioBufFormat = (AudioBufFormat) src;
        AudioCodecFormat encodeFormat = (AudioCodecFormat) dst;
        encodeFormat.sampleFmt = audioBufFormat.sampleFormat;
        encodeFormat.sampleRate = audioBufFormat.sampleRate;
        encodeFormat.channels = audioBufFormat.channels;
        return true;
    }

    @Override
    protected AudioBufFrame onFrameAvailable(AudioBufFrame frame) {
        if (frame == null || frame.buf == null) {
            return frame;
        }
        ByteBuffer buffer = mAudioBufferCache.poll(frame.buf.limit());
        if (buffer == null) {
            Log.w(TAG, "Audio frame dropped, size=" + frame.buf.limit() + " pts=" + frame.pts);
            return null;
        }
        AudioBufFrame outFrame = new AudioBufFrame(frame);
        buffer.put(outFrame.buf);
        buffer.flip();
        outFrame.buf.rewind();
        outFrame.buf = buffer;
        return outFrame;
    }

    @Override
    protected int doFrameAvailable(AudioBufFrame frame) {
        int ret = 0;
        if (frame != null && frame.buf != null) {
            if (mMute) {
                for (int i = 0; i < frame.buf.limit(); i++) {
                    frame.buf.put(i, (byte) 0);
                }
                frame.buf.rewind();
            }
            try {
                drainEncoder(false);
                fillEncoder(frame.buf, frame.pts * 1000);
            } catch (Exception e) {
                Log.e(TAG, "Encode frame failed!");
                ret = ENCODER_ERROR_UNKNOWN;
                e.printStackTrace();
            }
            mAudioBufferCache.offer(frame.buf);
        }
        return ret;
    }

    @Override
    protected AudioPacket getOutFrame(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        AudioPacket packet = new AudioPacket(mOutFormat, buffer,
                bufferInfo.presentationTimeUs / 1000);
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
            packet.flags |= AVConst.FLAG_END_OF_STREAM;
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0)
            packet.flags |= AVConst.FLAG_KEY_FRAME;
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
            packet.flags |= AVConst.FLAG_CODEC_CONFIG;
        return packet;
    }
}
