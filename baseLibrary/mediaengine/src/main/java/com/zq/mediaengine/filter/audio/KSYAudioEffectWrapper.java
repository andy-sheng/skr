package com.zq.mediaengine.filter.audio;

import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.util.LibraryLoader;

import java.nio.ByteBuffer;

/**
 * @hide
 */

class KSYAudioEffectWrapper {
    private AudioBufFormat mFormat;

    private long mInstance = 0;

    public KSYAudioEffectWrapper(int type) {
        mInstance = native_init();
        native_set_effect_type(mInstance, type);
    }

    public void setAudioFormat(AudioBufFormat format) {
        this.mFormat = format;
        native_set_audio_format(mInstance, format.sampleFormat, format.sampleRate, format.channels);
    }

    public void setEffectType(int type) {
        native_set_effect_type(mInstance, type);
    }

    public void setPitchLevel(int level) {
        native_set_pitch_level(mInstance, level);
    }

    public void addEffects(String name, int argc, String[] argv){
        int optionCount = argv.length;
        EffectOption[] options = new EffectOption[optionCount];
        for(int i = 0; i < argv.length; i++) {
            options[i] = new EffectOption(argv[i]);
        }
        native_add_effect(mInstance, name, argc, options);
    }

    public void removeEffects() {
        native_remove_effects(mInstance);
    }

    public void process(ByteBuffer buf) {
        if (buf != null && buf.limit() > 0) {
            native_process(mInstance, buf, buf.limit());
        }
    }

    public void release() {
      native_quit(mInstance);
    }

    public void attachTo(int idx, long ptr, boolean detach) {
        attachTo(mInstance, idx, ptr, detach);
    }

    public long getNativeInstance() {
        return mInstance;
    }

    public int read(ByteBuffer buffer, int size) {
        int ret =  native_read(mInstance, buffer, size);
        if (ret >= 0) {
            buffer.rewind();
            buffer.limit(ret);
        }
        return ret;
    }

    private native long native_init();
    private native void native_set_audio_format(long ptr,int sampleFmt, int sampleRate, int channels);
    private native void native_set_effect_type(long ptr,int type);
    private native void native_set_pitch_level(long ptr, int level);
    private native void native_add_effect(long ptr, String name, int argc, EffectOption[] argv);
    private native void native_remove_effects(long ptr);
    private native void native_process(long ptr, ByteBuffer buf, int size);
    private native void native_quit(long ptr);
    private native void attachTo(long instance, int idx, long ptr, boolean detach);
    private native int native_read(long instance, ByteBuffer buf, int size);

    static {
        LibraryLoader.load();
    }

    static public class EffectOption {
        String option;
        public EffectOption(String option) {
            this.option = option;
        }
    }
}
