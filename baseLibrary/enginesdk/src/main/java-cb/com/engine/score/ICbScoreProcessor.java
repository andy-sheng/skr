package com.engine.score;

import android.text.TextUtils;

import com.common.log.MyLog;

public class ICbScoreProcessor {

    public final static String TAG = "ICbScoreProcessor";

    static {
        try {
            MyLog.d(TAG, "loadLibrary");
            System.loadLibrary("native-lib1");
        } catch (Exception e) {
            MyLog.d(TAG, e);
            e.printStackTrace();
        }
    }

    private String melPath;

    public int init() {
        melPath = null;
        return 0;
    }

    public int process(byte[] samples, int length, int channels, int samplesPerSec, long currentTimeMills, String melPath) {
        //MyLog.d(TAG,"process" + " samples=" + samples + " length=" + length + " channels=" + channels + " samplesPerSec=" + samplesPerSec + " currentTimeMills=" + currentTimeMills + " melPath=" + melPath);
        boolean needScore = false;
        boolean restartEngine = false;
        if (!TextUtils.isEmpty(melPath) && currentTimeMills > 0) {
            needScore = true;
            if (!melPath.equals(this.melPath)) {
                this.melPath = melPath;
                restartEngine = true;
            } else {
                restartEngine = false;
            }
        }
        int r = process1(needScore, restartEngine, samples, length, channels, samplesPerSec, currentTimeMills, melPath);
        return r;
    }

    public int getScore() {
        return getScore1();
    }

    public int destroy() {
        melPath = null;
        destroyScoreProcessor();
        return 0;
    }

    public native int process1(boolean needScore, boolean restartEngine, byte[] samples, int length, int channels, int samplesPerSec, long currentTimeMills, String melPath);

    public native void destroyScoreProcessor();

    public native int getScore1();
}
