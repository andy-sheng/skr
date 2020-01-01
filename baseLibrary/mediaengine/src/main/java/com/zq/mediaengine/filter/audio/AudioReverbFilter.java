package com.zq.mediaengine.filter.audio;

import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

import java.nio.ByteBuffer;

/**
 * Audio reverb filter, with 5 acceptable level.
 */
public class AudioReverbFilter extends AudioFilterBase {
    private static final String TAG = "AudioReverbFilter";

    public static int AUDIO_REVERB_NONE = 0;

    public static int AUDIO_REVERB_LEVEL_1 = 1;
    public static int AUDIO_REVERB_LEVEL_2 = 2;
    public static int AUDIO_REVERB_LEVEL_3 = 3;
    public static int AUDIO_REVERB_LEVEL_4 = 4;
    public static int AUDIO_REVERB_LEVEL_5 = 5;

    // from Audacity
    public static int AUDIO_REVERB_PRESET_BATHROOM = 10;
    public static int AUDIO_REVERB_PRESET_SMALLROOM = 11;
    public static int AUDIO_REVERB_PRESET_MEDIUMROOM = 12;
    public static int AUDIO_REVERB_PRESET_LARGEROOM = 13;
    public static int AUDIO_REVERB_PRESET_CHURCHHALL = 14;

    public static int AUDIO_REVERB_RNB = 20;
    public static int AUDIO_REVERB_ROCK = 21;
    public static int AUDIO_REVERB_POPULAR = 22;
    public static int AUDIO_REVERB_DANCE = 23;
    public static int AUDIO_REVERB_NEW_CENT = 24;

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
            if (mAudioReverbWrap != null) {
                mAudioReverbWrap.setReverbLevel(mReverbLevel);
            }
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
