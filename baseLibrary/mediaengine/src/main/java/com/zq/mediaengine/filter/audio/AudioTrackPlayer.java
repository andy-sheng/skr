package com.zq.mediaengine.filter.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.nio.ByteBuffer;

/**
 * PCM player use AudioTrack.
 */

public class AudioTrackPlayer implements IPcmPlayer {

    private AudioTrack mAudioTrack;
    private short[] mPcm;
    private boolean mMute;
    private boolean mStart;

    @Override
    public long getNativeInstance() {
        return 0;
    }

    @Override
    public synchronized int config(int sampleRate, int channels,
                                   int bufferSamples, int fifoSizeInMs) {
        if (mAudioTrack != null) {
            mAudioTrack.release();
        }
        int channel = (channels == 1) ? AudioFormat.CHANNEL_OUT_MONO :
                AudioFormat.CHANNEL_OUT_STEREO;
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channel,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channel,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
        if (mMute) {
            mAudioTrack.setStereoVolume(0.0f, 0.0f);
        }
        if (mStart) {
            mAudioTrack.play();
        }
        return 0;
    }

    @Override
    public synchronized void setMute(boolean mute) {
        if (mAudioTrack != null) {
            float vol = mute ? 0.0f : 1.0f;
            mAudioTrack.setStereoVolume(vol, vol);
        }
        mMute = mute;
    }

    @Override
    public synchronized int start() {
        if (mAudioTrack != null) {
            mAudioTrack.play();
        }
        mStart = true;
        return 0;
    }

    @Override
    public synchronized int stop() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
        }
        mStart = false;
        mPcm = null;
        return 0;
    }

    @Override
    public int pause() {
        if (mAudioTrack != null) {
            mAudioTrack.pause();
        }
        return 0;
    }

    @Override
    public int resume() {
        if (mAudioTrack != null) {
            mAudioTrack.play();
        }
        return 0;
    }

    @Override
    public synchronized int write(ByteBuffer buffer) {
        if (buffer == null || mAudioTrack == null) {
            return 0;
        }

        int len = buffer.limit() / 2;
        if (mPcm == null || mPcm.length < len) {
            mPcm = new short[len];
        }
        buffer.asShortBuffer().get(mPcm, 0, len);
        return mAudioTrack.write(mPcm, 0, len);
    }

    @Override
    public synchronized void release() {
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
        mPcm = null;
    }
}
