package com.engine.score;

import android.os.Message;
import android.text.TextUtils;

import com.common.engine.ScoreConfig;
import com.common.log.MyLog;
import com.common.utils.CustomHandlerThread;

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

    CustomHandlerThread mCustomHandlerThread;

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
        int r1 = process1(needScore, restartEngine, samples, length, channels, samplesPerSec, currentTimeMills, melPath);
        if (ScoreConfig.isMelp2Enable()) {
            if (mCustomHandlerThread == null) {
                mCustomHandlerThread = new CustomHandlerThread("getScore2") {
                    @Override
                    protected void processMessage(Message var1) {
                        if (var1.what == 1) {
                            Holder holder = (Holder) var1.obj;
                            process2(holder.needScore, holder.restartEngine, holder.samples, holder.length, holder.channels, holder.samplesPerSec, holder.currentTimeMills, holder.melPath);
                        } else if (var1.what == 2) {
                            Score2Callback score2Callback = (Score2Callback) var1.obj;
                            if (score2Callback != null) {
                                int score2 = getScore2();
                                score2Callback.onGetScore(var1.arg1, score2);
                            }
                        }
                    }
                };
            }
            Message msg = mCustomHandlerThread.obtainMessage();
            msg.what = 1;
            msg.obj = new Holder(needScore, restartEngine, samples, length, channels, samplesPerSec, currentTimeMills, melPath);
            mCustomHandlerThread.sendMessage(msg);
        }

        return r1;
    }

    public int getScoreV1() {
        return getScore1();
    }

    public void getScoreV2(int lineNum, Score2Callback score2Callback) {
        if (mCustomHandlerThread != null) {
            Message msg = mCustomHandlerThread.obtainMessage();
            msg.what = 2;
            msg.arg1 = lineNum;
            msg.obj = score2Callback;
            mCustomHandlerThread.sendMessage(msg);
        }
    }

    public int destroy() {
        melPath = null;
        destroyScoreProcessor();
        return 0;
    }

    public native int process1(boolean needScore, boolean restartEngine, byte[] samples, int length, int channels, int samplesPerSec, long currentTimeMills, String melPath);

    public native int getScore1();

    public native int process2(boolean needScore, boolean restartEngine, byte[] samples, int length, int channels, int samplesPerSec, long currentTimeMills, String melPath);

    public native int getScore2();

    public native void destroyScoreProcessor();

    public static class Holder {
        boolean needScore;
        boolean restartEngine;
        byte[] samples;
        int length;
        int channels;
        int samplesPerSec;
        long currentTimeMills;
        String melPath;

        public Holder(boolean needScore, boolean restartEngine, byte[] samples, int length, int channels, int samplesPerSec, long currentTimeMills, String melPath) {
            this.needScore = needScore;
            this.restartEngine = restartEngine;
            this.samples = samples;
            this.length = length;
            this.channels = channels;
            this.samplesPerSec = samplesPerSec;
            this.currentTimeMills = currentTimeMills;
            this.melPath = melPath;
        }
    }
}
