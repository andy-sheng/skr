package com.zq.mediaengine.filter.audio;

import android.util.Log;

import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

import java.nio.ByteBuffer;

/**
 * audio effect
 */

public class AudioEffectFilter extends AudioFilterBase {
    private static final String TAG = "AudioEffectFilter";
    private static final boolean VERBOSE = false;

    /*
      pitch level from -3 to 3
     */
    public static int AUDIO_PITCH_LEVEL_1 = -3;
    public static int AUDIO_PITCH_LEVEL_2 = -2;
    public static int AUDIO_PITCH_LEVEL_3 = -1;
    public static int AUDIO_PITCH_LEVEL_4 = 0;
    public static int AUDIO_PITCH_LEVEL_5 = 1;
    public static int AUDIO_PITCH_LEVEL_6 = 2;
    public static int AUDIO_PITCH_LEVEL_7 = 3;

    //变调
    public static int AUDIO_EFFECT_TYPE_PITCH = 9;
    //萝莉
    public static int AUDIO_EFFECT_TYPE_FEMALE = 10;
    //大叔
    public static int AUDIO_EFFECT_TYPE_MALE = 11;
    //庄严
    public static int AUDIO_EFFECT_TYPE_HEROIC = 12;
    //机器人
    public static int AUDIO_EFFECT_TYPE_ROBOT = 13;

    private AudioEffectWrapper mWrapper;
    private AudioBufFormat mInputFormat;

    private int mAudioEffectType =  AUDIO_EFFECT_TYPE_PITCH;
    private int mAudioPitchLevel = AUDIO_PITCH_LEVEL_4;

    public AudioEffectFilter(int type) {
        mAudioEffectType = type;
        mWrapper = new AudioEffectWrapper(type);
    }

    /**
     * set audio effect type
     * @param type
     */
    public void setAudioEffectType(int type) {
        this.mAudioEffectType = type;
    }

    public int getAudioEffectType() {
        return mAudioEffectType;
    }

    /**
     * set pitch level
     * @param level, pitch level from -3 to 3
     */
    public void setPitchLevel(int level) {
        mAudioEffectType = AUDIO_EFFECT_TYPE_PITCH;
        mAudioPitchLevel = level;
        mWrapper.setPitchLevel(level);
    }

    public int getPitchLevel() {
        return mAudioPitchLevel;
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        mInputFormat = format;
        mWrapper.setAudioFormat(format);
        mWrapper.setEffectType(mAudioEffectType);
        return format;
    }

    @Override
    protected void doRelease() {
        if (mWrapper != null) {
            mWrapper.release();
            mWrapper = null;
        }
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        if(mWrapper != null) {
            long start = System.nanoTime() / 1000 / 1000;
            mWrapper.process(frame.buf);
            long end = System.nanoTime() / 1000 / 1000;
            if (VERBOSE) {
                Log.d(TAG, "process audio cost time: " + (end - start));
            }
        }
        return frame;
    }

    @Override
    protected long getNativeInstance() {
        return mWrapper.getNativeInstance();
    }

    @Override
    protected void attachTo(int idx, long ptr, boolean detach) {
        mWrapper.attachTo(idx, ptr, detach);
    }

    @Override
    protected int readNative(ByteBuffer buffer, int size) {
        return mWrapper.read(buffer, size);
    }
}
