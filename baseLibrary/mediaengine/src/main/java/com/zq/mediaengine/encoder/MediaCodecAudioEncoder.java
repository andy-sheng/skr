package com.zq.mediaengine.encoder;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.AudioEncodeConfig;
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
    private static final int AUDIO_CACHE_ITEM_SIZE = 8*1024;

    private FrameBufferCache mAudioBufferCache;

    public MediaCodecAudioEncoder() {
        mAudioBufferCache = new FrameBufferCache(AUDIO_CACHE_NUM, AUDIO_CACHE_ITEM_SIZE);
    }

    @Override
    public int getEncoderType() {
        return AVConst.MEDIA_TYPE_AUDIO;
    }

    @Override
    protected int doStart(Object encodeConfig) {
        AudioEncodeConfig config = (AudioEncodeConfig) encodeConfig;

        int channel;
        switch (config.channels) {
            case 1:
                channel = AudioFormat.CHANNEL_IN_MONO;
                break;
            case 2:
                channel = AudioFormat.CHANNEL_IN_STEREO;
                break;
            default:
                throw new IllegalArgumentException("Invalid channel count. Must be 1 or 2");
        }

        String mime;
        switch (config.codecId) {
            case AVConst.CODEC_ID_AAC:
                mime = "audio/mp4a-latm";
                break;
            default:
                throw new IllegalArgumentException("Only aac supported");
        }

        int audioProfile = config.profile;
        if (audioProfile == AVConst.PROFILE_AAC_HE_V2 && config.channels == 1) {
            audioProfile = AVConst.PROFILE_AAC_HE;
            Log.w(TAG, "set aac_he_v2 for mono audio, fallback to aac_he");
        }

        int profile;
        switch (audioProfile) {
            case AVConst.PROFILE_AAC_HE:
                profile = MediaCodecInfo.CodecProfileLevel.AACObjectHE;
                break;
            case AVConst.PROFILE_AAC_HE_V2:
                profile = MediaCodecInfo.CodecProfileLevel.AACObjectHE_PS;
                break;
            case AVConst.PROFILE_AAC_LOW:
            default:
                profile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
                break;
        }

        MediaFormat mediaFormat = MediaFormat.createAudioFormat(mime, config.sampleRate, channel);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, profile);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, config.channels);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, config.bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);
        mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 0);

        try {
            mEncoder = MediaCodec.createEncoderByType(mime);
            mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncoder.start();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start MediaCodec audio encoder");
            e.printStackTrace();
            return ENCODER_ERROR_UNSUPPORTED;
        }

        mBufferInfo = new MediaCodec.BufferInfo();
        mOutConfig = null;
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

        AudioPacket packet = new AudioPacket((AudioEncodeConfig) mOutConfig, null, 0);
        packet.flags |= AVConst.FLAG_END_OF_STREAM;
        onEncodedFrame(packet);
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
        int ret = 0;
        if (frame != null && frame.buf != null) {
            if (mMute) {
                for (int i = 0; i < frame.buf.limit(); i++) {
                    frame.buf.put(i, (byte)0);
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
    protected void updateOutFormat(MediaFormat mediaFormat) {
        mOutConfig = new AudioEncodeConfig((AudioEncodeConfig) mEncodeConfig);
    }

    @Override
    protected AudioPacket getOutFrame(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        AudioPacket packet = new AudioPacket((AudioEncodeConfig) mOutConfig, buffer,
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
