package com.zq.mediaengine.filter.audio;

import android.content.Context;
import android.util.Log;

import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.util.audio.AudioUtil;

import java.nio.ByteBuffer;

/**
 * Audio preview use openSL ES.
 */

public class AudioPreview extends AudioFilterBase {
    private static final String TAG = "AudioPreview";

    private Context mContext;
    private IPcmPlayer mPcmPlayer;
    private boolean mEnableLowLatency;
    private boolean mBlockingMode;

    public AudioPreview(Context context) {
        init(context, true);
    }

    public AudioPreview(Context context, boolean enableLowLatency) {
        init(context, enableLowLatency);
    }

    private void init(Context context, boolean enableLowLatency) {
        mContext = context;
        mEnableLowLatency = enableLowLatency;
        if (enableLowLatency) {
            mPcmPlayer = new AudioSLPlayer();
        } else {
            mPcmPlayer = new AudioTrackPlayer();
        }
    }

    public boolean isEnableLowLatency() {
        return mEnableLowLatency;
    }

    public void start() {
        mPcmPlayer.start();
    }

    public void stop() {
        mPcmPlayer.stop();
    }

    public void pause() {
        mPcmPlayer.pause();
    }

    public void resume() {
        mPcmPlayer.resume();
    }

    public void setMute(boolean mute) {
        mPcmPlayer.setMute(mute);
    }

    public void setVolume(float volume) {
        mPcmPlayer.setVolume(volume);
    }

    /**
     * Set use blocking mode or not.
     *
     * @param blockingMode true to enable, false to disable
     */
    public void setBlockingMode(boolean blockingMode) {
        mBlockingMode = blockingMode;
    }

    @Override
    protected long getNativeInstance() {
        return mPcmPlayer.getNativeInstance();
    }

    @Override
    protected int readNative(ByteBuffer buffer, int size) {
        return mPcmPlayer.read(buffer, size);
    }

    @Override
    protected void attachTo(int idx, long ptr, boolean detach) {
        mPcmPlayer.attachTo(idx, ptr, detach);
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        int atomSize = AudioUtil.getNativeBufferSize(mContext, format.sampleRate);
        int ret = mPcmPlayer.config(format.sampleFormat, format.sampleRate, format.channels, atomSize, 200);
        if (ret < 0) {
            return null;
        }
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        mPcmPlayer.write(frame.buf, !mBlockingMode);
        return frame;
    }

    @Override
    protected void doRelease() {
        Log.i(TAG, "doRelease");
        mPcmPlayer.stop();
        mPcmPlayer.release();
    }
}
