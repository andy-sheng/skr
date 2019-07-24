package com.component.mediaengine.filter.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.component.mediaengine.framework.AVConst;

import java.nio.ByteBuffer;

/**
 * PCM player use AudioTrack.
 */

public class AudioTrackPlayer implements IPcmPlayer {
    private final static String TAG = "AudioTrackPlayer";

    private AudioTrack mAudioTrack;
    private int mSampleRate;
    private short[] mPcm;
    private boolean mMute;
    private boolean mStart;

    @Override
    public long getNativeInstance() {
        return 0;
    }

    @Override
    public synchronized int config(int sampleFmt, int sampleRate, int channels,
                                   int bufferSamples, int fifoSizeInMs) {
        if (sampleFmt != AVConst.AV_SAMPLE_FMT_S16) {
            Log.e(TAG, "AudioTrackPlayer only support SAMPLE_FMT_S16!");
            return -1;
        }
        if (mAudioTrack != null) {
            mAudioTrack.release();
        }
        mSampleRate = sampleRate;
        int channel = (channels == 1) ? AudioFormat.CHANNEL_OUT_MONO :
                AudioFormat.CHANNEL_OUT_STEREO;
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channel,
                AudioFormat.ENCODING_PCM_16BIT);
        Log.e(TAG, "minBufferSize: " + minBufferSize);
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
    public int flush() {
        if (mAudioTrack != null) {
            mAudioTrack.pause();
            mAudioTrack.flush();
            mAudioTrack.play();
        }
        return 0;
    }

    @Override
    public long getPosition() {
        if (mAudioTrack != null) {
            long samples = mAudioTrack.getPlaybackHeadPosition();
            return samples * 1000 / mSampleRate;
        }
        return 0;
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
