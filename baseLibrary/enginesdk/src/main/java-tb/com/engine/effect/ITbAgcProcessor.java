package com.engine.effect;


import com.common.log.MyLog;

import java.nio.ByteBuffer;

public class ITbAgcProcessor {
    public final static String TAG = "ITbEffectProcessor";

    static {
        try {
            MyLog.d(TAG, "loadLibrary");
            System.loadLibrary("native-lib2");
        } catch (Exception e) {
            MyLog.d(TAG, e);
            e.printStackTrace();
        }
    }

    public native int init();

    public int processV1(byte[] samples, int length, int channels, int samplesPerSec) {
         return process(samples, null, length,channels,samplesPerSec);
    }

    public int processV1(ByteBuffer byteBuffer, int length, int channels, int samplesPerSec) {
        return process(null, byteBuffer, length, channels, samplesPerSec);
    }

    private native int process(byte[] samples, ByteBuffer byteBuffer, int length, int channels, int samplesPerSec);

    public native int destroyAgcProcessor();


}
