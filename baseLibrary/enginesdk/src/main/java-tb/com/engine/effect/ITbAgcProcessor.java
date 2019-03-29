package com.engine.effect;


import com.common.log.MyLog;

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

    public int processV1( byte[] samples, int length, int channels, int samplesPerSec) {
         process(samples,length,channels,samplesPerSec);
         return 0;
    }

    public native int process(byte[] samples, int length, int channels, int samplesPerSec);


    public native int destroyAgcProcessor();


}
