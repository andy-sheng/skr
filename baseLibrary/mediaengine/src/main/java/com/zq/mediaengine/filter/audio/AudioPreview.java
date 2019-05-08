package com.zq.mediaengine.filter.audio;

import android.content.Context;

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
    private AudioSLPlayer mAudioSLPlayer;
    private boolean mBlockingMode;

    public AudioPreview(Context context) {
        mContext = context;
        mAudioSLPlayer = new AudioSLPlayer();
    }

    public void start() {
        mAudioSLPlayer.start();
    }

    public void stop() {
        mAudioSLPlayer.stop();
    }

    public void pause() {
        mAudioSLPlayer.pause();
    }

    public void resume() {
        mAudioSLPlayer.resume();
    }

    public void setMute(boolean mute) {
        mAudioSLPlayer.setMute(mute);
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
        return mAudioSLPlayer.getNativeInstance();
    }

    @Override
    protected int readNative(ByteBuffer buffer, int size) {
        return mAudioSLPlayer.read(buffer, size);
    }

    @Override
    protected void attachTo(int idx, long ptr, boolean detach) {
        mAudioSLPlayer.attachTo(idx, ptr, detach);
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        int atomSize = AudioUtil.getNativeBufferSize(mContext, format.sampleRate);
        mAudioSLPlayer.config(format.sampleRate, format.channels, atomSize, 200);
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        mAudioSLPlayer.write(frame.buf, !mBlockingMode);
        return frame;
    }

    @Override
    protected void doRelease() {
        mAudioSLPlayer.stop();
        mAudioSLPlayer.release();
    }
}
