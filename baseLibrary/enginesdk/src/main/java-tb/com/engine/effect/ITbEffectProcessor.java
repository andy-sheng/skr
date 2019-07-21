package com.engine.effect;


import com.common.log.MyLog;

import java.nio.ByteBuffer;

public class ITbEffectProcessor {
    public static final String TAG = "ITbEffectProcessor";

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

    public int process(int type, byte[] samples, int length, int channels, int samplesPerSec) {
        if (type == 1) {
            int r = process1(samples, null, length, channels, samplesPerSec);
            return r;
        } else {
            int r = process2(samples, null, length, channels, samplesPerSec);
            return r;
        }
    }

    public int process(int type, ByteBuffer buffer, int length, int channels, int samplesPerSec) {
        byte[] byteArray = null;
        ByteBuffer byteBuffer = buffer;
        if (buffer != null && buffer.hasArray()) {
            byteArray = buffer.array();
            byteBuffer = null;
        }
        if (type == 1) {
            int r = process1(byteArray, byteBuffer, length, channels, samplesPerSec);
            return r;
        } else {
            int r = process2(byteArray, byteBuffer, length, channels, samplesPerSec);
            return r;
        }
    }

    public native int process1(byte[] samples, ByteBuffer byteBuffer, int length, int channels, int samplesPerSec);

    public native int process2(byte[] samples, ByteBuffer byteBuffer, int length, int channels, int samplesPerSec);

    public native int destroyEffectProcessor();


}
