package com.component.mediaengine.filter.audio;

import android.util.Log;

import com.component.mediaengine.framework.AVConst;
import com.component.mediaengine.framework.AudioBufFormat;
import com.component.mediaengine.util.LibraryLoader;

import java.nio.ByteBuffer;

/**
 * Created by zanxiaofei on 17/3/16.
 */

public class APMWrapper {
    private final static String TAG = "APMWrapper";
    public final static int NS_LEVEL_LOW = 0;
    public final static int NS_LEVEL_MODERATE = 1;
    public final static int NS_LEVEL_HIGH = 2;
    public final static int NS_LEVEL_VERYHIGH = 3;

    public final static int AEC_ROUTING_MODE_HEADSET = 0;
    public final static int AEC_ROUTING_MODE_EARPIECE = 1;
    public final static int AEC_ROUTING_MODE_LOUD_EARPIECE = 2;
    public final static int AEC_ROUTING_MODE_SPEAKER_PHONE = 3;
    public final static int AEC_ROUTING_MODE_LOUD_SPEAKER_PHONE = 4;

    public final static int APM_SAMPLE_RATE = 48000;
    public final static int APM_CHANNEL_NUM = 2;
    public final static int APM_SAMPLE_FORMAT = AVConst.AV_SAMPLE_FMT_S16;

    private final static int UNINIT = 0;

    public static boolean mNativeLibraryLoad = true;

    private long mAPMWrapperInstance = UNINIT;

    private AudioBufFormat mAPMProcessFormat;

    public APMWrapper() {
        if (!mNativeLibraryLoad) {
            Log.e(TAG, "native library not loaded!");
            return;
        }
        mAPMWrapperInstance = create();
        mAPMProcessFormat = new AudioBufFormat(APM_SAMPLE_FORMAT, APM_SAMPLE_RATE, APM_CHANNEL_NUM);
        if (mAPMWrapperInstance == 0) {
            Log.e(TAG, "apm create failed ，ret " + mAPMWrapperInstance);
        }
    }

    public long getNativeInstance() {
        return mAPMWrapperInstance;
    }

    public AudioBufFormat getAPMFormat() {
        return mAPMProcessFormat;
    }

    public ByteBuffer processStream(int idx, ByteBuffer data) {
        if (mAPMWrapperInstance == UNINIT || data == null) {
            return null;
        }
        return processStream(mAPMWrapperInstance, idx, data, data.limit());
    }

    private int enableHighPassFilter(boolean enable) {
        if (mAPMWrapperInstance == UNINIT) {
            return -1;
        }
        return enableHighPassFilter(mAPMWrapperInstance, enable);
    }

    public int enableNs(boolean enable) {
        if (mAPMWrapperInstance == UNINIT) {
            return -1;
        }
        return enableNs(mAPMWrapperInstance, enable);
    }

    public int setNsLevel(int level) {
        if (mAPMWrapperInstance == UNINIT) {
            return -1;
        }

        return setNsLevel(mAPMWrapperInstance, level);
    }

    private int enableVAD(boolean enable) {
        if (mAPMWrapperInstance == UNINIT) {
            return -1;
        }
        return enableVAD(mAPMWrapperInstance, enable);
    }

    private int setVADLikelihood(int likelihood) {
        if (mAPMWrapperInstance == UNINIT) {
            return -1;
        }
        return setVADLikelihood(mAPMWrapperInstance, likelihood);
    }

    public int enableAECM(boolean enable) {
        if (mAPMWrapperInstance == UNINIT) {
            return -1;
        }
        return enableAECM(mAPMWrapperInstance, enable);
    }

    public int enableAEC(boolean enable) {
        if (mAPMWrapperInstance == UNINIT) {
            return -1;
        }
        return enableAEC(mAPMWrapperInstance, enable);
    }

    public int setRoutingMode(int mode) {
        if (mAPMWrapperInstance == UNINIT) {
            return -1;
        }
        return setRoutingMode(mAPMWrapperInstance, mode);
    }

    public int setStreamDelay(int delay) {
        if (mAPMWrapperInstance == UNINIT) {
            return -1;
        }
        return setStreamDelay(mAPMWrapperInstance, delay);
    }

    public int config(int idx, int sampleFmt, int samplerate, int channels) {
        if (mAPMWrapperInstance == UNINIT) {
            return -1;
        }
        mAPMProcessFormat.sampleRate = samplerate;
        mAPMProcessFormat.channels = channels;
        return config(mAPMWrapperInstance, idx, sampleFmt, samplerate, channels);
    }

    public void attachTo(int idx, long ptr, boolean detach) {
        attachTo(mAPMWrapperInstance, idx, ptr, detach);
    }

    public int read(ByteBuffer buffer, int size) {
        int ret = read(mAPMWrapperInstance, buffer, size);
        if (ret >= 0) {
            buffer.rewind();
            buffer.limit(ret);
        }
        return ret;
    }

    public void release() {
        if (mAPMWrapperInstance == UNINIT) {
            return;
        }
        release(mAPMWrapperInstance);
    }


    private native long create();

    private native ByteBuffer processStream(long instance, int idx, ByteBuffer data, int size);

    private native int enableHighPassFilter(long instance, boolean enable);

    private native int enableNs(long instance, boolean enable);

    private native int setNsLevel(long instance, int level);

    private native int enableVAD(long instance, boolean enable);

    private native int setVADLikelihood(long instance, int likelihood);

    private native int enableAECM(long instance, boolean enable);

    private native int enableAEC(long instance, boolean enable);

    private native int setRoutingMode(long instance, int mode);

    private native int setStreamDelay(long instance, int delay);

    private native int config(long instance, int idx, int sampleFmt, int samplerate, int channels);

    private native void attachTo(long instance, int idx, long ptr, boolean detach);

    private native int read(long instance, ByteBuffer buffer, int size);

    private native void release(long instance);

    static {
        LibraryLoader.load();
        try {
            System.loadLibrary("zqapm");
        } catch (UnsatisfiedLinkError error) {
            mNativeLibraryLoad = false;
            Log.e(TAG, "No libzqapm.so! Please check ");
        }
    }
}
