package com.engine.effect;


import com.common.log.MyLog;

public class ITbEffectProcessor {
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

    public int process(int type, byte[] samples, int length, int channels, int samplesPerSec) {
        if (type == 1) {
            int r = process1(samples, length, channels, samplesPerSec);
            return r;
        } else {
            int r = process2(samples, length, channels, samplesPerSec);
            return r;
        }
    }

    public native int process1(byte[] samples, int length, int channels, int samplesPerSec);

    public native int process2(byte[] samples, int length, int channels, int samplesPerSec);

    public native int destroyEffectProcessor();


}
