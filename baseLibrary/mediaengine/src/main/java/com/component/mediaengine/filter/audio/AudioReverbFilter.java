package com.component.mediaengine.filter.audio;

import com.component.mediaengine.framework.AudioBufFormat;
import com.component.mediaengine.framework.AudioBufFrame;

import java.nio.ByteBuffer;

/**
 * Audio reverb filter, with 5 acceptable level.
 */
public class AudioReverbFilter extends AudioFilterBase {
    public static int AUDIO_REVERB_LEVEL_1 = 1;
    public static int AUDIO_REVERB_LEVEL_2 = 2;
    public static int AUDIO_REVERB_LEVEL_3 = 3;
    public static int AUDIO_REVERB_LEVEL_4 = 4;
    public static int AUDIO_REVERB_LEVEL_5 = 5;

    private int mReverbLevel = AUDIO_REVERB_LEVEL_3;
    private boolean mEffect = false;
    private AudioReverbWrap mAudioReverbWrap;

    public AudioReverbFilter() {
        mAudioReverbWrap = new AudioReverbWrap();
    }

    /**
     * Set audio reverb level, default {@link #AUDIO_REVERB_LEVEL_3}.
     *
     * @param revertLevel reverb level to be set
     * @see #AUDIO_REVERB_LEVEL_1
     * @see #AUDIO_REVERB_LEVEL_2
     * @see #AUDIO_REVERB_LEVEL_3
     * @see #AUDIO_REVERB_LEVEL_4
     */
    public void setReverbLevel(int revertLevel) {
        if (revertLevel != mReverbLevel) {
            mReverbLevel = revertLevel;
            mAudioReverbWrap.setReverbLevel(mReverbLevel);
        }
    }

    public void setTakeEffect(boolean effect) {
        mEffect = true;
    }

    public int getReverbType() {
        return mReverbLevel;
    }

    @Override
    protected long getNativeInstance() {
        return mAudioReverbWrap.getNativeInstance();
    }

    @Override
    protected void attachTo(int idx, long ptr, boolean detach) {
        mAudioReverbWrap.attachTo(idx, ptr, detach);
    }

    @Override
    protected int readNative(ByteBuffer buffer, int size) {
        return mAudioReverbWrap.read(buffer, size);
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        mAudioReverbWrap.config(format.sampleFormat, format.sampleRate, format.channels);
        mAudioReverbWrap.setReverbLevel(mReverbLevel);
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        mAudioReverbWrap.processReverb(frame.buf);
        return frame;
    }

    @Override
    protected void doRelease() {
        mEffect = false;
        if (mAudioReverbWrap != null) {
            mAudioReverbWrap.release();
            mAudioReverbWrap = null;
        }
    }
}
