package com.engine.score;

import android.text.TextUtils;

import com.common.log.MyLog;

import java.io.File;

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
        melFileExist = false;
        checkMelFileTs = 0;
        return 0;
    }

    boolean melFileExist = false; // 检查mel文件是否存在
    long checkMelFileTs = 0;// 如果不存在设置检查间隔

    public int process(byte[] samples, int length, int channels, int samplesPerSec, long currentTimeMills, String melPath) {
        //MyLog.d(TAG,"process" + " samples=" + samples + " length=" + length + " channels=" + channels + " samplesPerSec=" + samplesPerSec + " currentTimeMills=" + currentTimeMills + " melPath=" + melPath);
        boolean needScore = false;
        boolean restartEngine = false;
        if (!TextUtils.isEmpty(melPath) && currentTimeMills > 0) {
            if (!melFileExist) {
                long now = System.currentTimeMillis();
                if (now - checkMelFileTs > 3000) {
                    File file = new File(melPath);
                    if (file != null && file.exists()) {
                        melFileExist = true;
                    }
                }
                checkMelFileTs = now;
            }
            if (!melFileExist) {
                needScore = false;
            } else {
                needScore = true;
                if (!melPath.equals(this.melPath)) {
                    this.melPath = melPath;
                    restartEngine = true;
                } else {
                    restartEngine = false;
                }
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
