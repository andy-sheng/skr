package com.engine.arccloud;

import android.text.TextUtils;

import com.acrcloud.rec.ACRCloudClient;
import com.acrcloud.rec.ACRCloudConfig;
import com.acrcloud.rec.ACRCloudResult;
import com.acrcloud.rec.IACRCloudListener;
import com.acrcloud.rec.utils.ACRCloudLogger;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class ArcCloudManager implements IACRCloudListener {
    public final static String TAG = "ArcCloudManager";

    static final int BUFFER_LEN = 10 * 44100 * 2;

    byte[] mBuffer = new byte[BUFFER_LEN];
    int mLength = 0;
    int mSampleRate = -1;
    int mChannels = -1;
    int mLineNo = 0;
    RecognizeConfig mRecognizeConfig;

    static final boolean DEBUG = false && MyLog.isDebugLogOpen();

    @Override
    public void onResult(ACRCloudResult acrCloudResult) {
        MyLog.d(TAG, "onResult" + " acrCloudResult=" + acrCloudResult.getResult());
    }

    @Override
    public void onVolumeChanged(double v) {

    }

//    private static class ArcCloudManagerHolder {
//        private static final ArcCloudManager INSTANCE = new ArcCloudManager();
//    }

    private ACRCloudClient mClient;
    private boolean mInited = false;
    private boolean mProcessing = false;
    private long mLastStartTs = System.currentTimeMillis();


    void tryInit() {
        if (mClient == null) {
            synchronized (this) {
                if (mClient == null) {
                    MyLog.d(TAG, "init");
                    ACRCloudConfig mConfig = new ACRCloudConfig();
                    mConfig.acrcloudListener = this;
                    mConfig.context = U.app();
                    // Please create project in "http://console.acrcloud.cn/service/avr".
                    mConfig.host = "identify-cn-north-1.acrcloud.com";
//        mConfig.dbPath = path; // offline db path, you can change it with other path which this app can access.
                    mConfig.accessKey = "54b5d9b267c9f900c54dde6e6e86ebe4";
                    mConfig.accessSecret = "QsE01a4rjRr7DwRNsxSkyQL2U7EIfhq2pOFhJWPh";
                    // auto recognize access key
                    mConfig.hostAuto = "";
                    mConfig.accessKeyAuto = "";
                    mConfig.accessSecretAuto = "";
                    mConfig.recorderConfig.rate = 8000;
                    mConfig.recorderConfig.channels = 1;
                    // If you do not need volume callback, you set it false.
                    mConfig.recorderConfig.isVolumeCallback = true;
                    mConfig.recorderConfig.reservedRecordBufferMS = 0;
                    mConfig.recorderConfig.recordOnceMaxTimeMS = 0;
                    this.mClient = new ACRCloudClient();
                    this.mInited = this.mClient.initWithConfig(mConfig);
                    ACRCloudLogger.setLog(true);
                }
            }
        }
    }
//
//    public void startRecognize() {
//        tryInit();
//        if (!this.mInited) {
//            MyLog.d(TAG, "start");
//            return;
//        }
//        if (!mProcessing) {
//            mProcessing = true;
//            if (this.mClient == null || !this.mClient.startRecognize()) {
//                mProcessing = false;
//            }
//            mLastStartTs = System.currentTimeMillis();
//        }
//    }
//
//    public void stopRecognize() {
//        MyLog.d(TAG, "stopRecognize");
//
//        if (mProcessing && this.mClient != null) {
//            this.mClient.stopRecordToRecognize();
//        }
//        mProcessing = false;
//    }

    public void startRecognize(RecognizeConfig recognizeConfig) {
        MyLog.d(TAG, "startCollect" + " recognizeConfig=" + recognizeConfig);
        // 开始积攒
        mProcessing = false;
        this.mRecognizeConfig = recognizeConfig;
        // 停止积攒
        setLen(0);
        if (DEBUG) {
            File file = new File(U.getAppInfoUtils().getSubDirPath("acr"));
            U.getFileUtils().deleteAllFiles(file);
        }
    }

    public void setRecognizeListener(ArcRecognizeListener recognizeConfig) {
        if (this.mRecognizeConfig != null) {
            this.mRecognizeConfig.setResultListener(recognizeConfig);
        }
    }

    public void stopRecognize() {
        MyLog.d(TAG, "stopRecognize");
        // 停止积攒
//        setLen(0);
        this.mRecognizeConfig = null;
        mProcessing = false;
    }

    public void putPool(byte[] buffer, int sampleRate, int nChannels) {
        //MyLog.d(TAG, "putPool" + " buffer=" + buffer.length + " sampleRate=" + sampleRate + " nChannels=" + nChannels);
        if (mRecognizeConfig == null) {
            return;
        }
        byte[] newBuffer = buffer;
        if (nChannels == 2) {
            nChannels = 1;
            newBuffer = new byte[buffer.length / 2];
            for (int i = 0; i < buffer.length / 4; i++) {
                newBuffer[i * 2] = buffer[i * 4];// 0123456-->0246
                newBuffer[i * 2 + 1] = buffer[i * 4 + 1];
            }
        }

//        {
//            File file = new File(U.getAppInfoUtils().getFilePathInSubDir("acr", "line.pcm"));
////            if (file != null) {
////                file.delete();
////            }
//            if (!file.getParentFile().exists()) {
//                file.getParentFile().mkdirs();
//            }
//            BufferedSink bufferedSink = null;
//            try {
//                Sink sink = Okio.appendingSink(file);
//                bufferedSink = Okio.buffer(sink);
//                bufferedSink.write(newBuffer);
//                MyLog.d(TAG, "写入文件 path:" + file.getAbsolutePath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                if (null != bufferedSink) {
//                    bufferedSink.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if(true){
//            return;
//        }

        RecognizeConfig recognizeConfig = mRecognizeConfig;

        if (recognizeConfig != null) {
            if (recognizeConfig.mode == RecognizeConfig.MODE_AUTO) {
                if (recognizeConfig.getAutoTimes() <= 0) {
                    return;
                }
            }

            if (mSampleRate < 0) {
                mSampleRate = sampleRate;
            }
            if (mChannels < 0) {
                mChannels = nChannels;
            }
            int tl = mLength;
            if (tl + newBuffer.length <= BUFFER_LEN) {
                // buffer还没满，足够容纳，那就放呗
                System.arraycopy(newBuffer, 0, mBuffer, tl, newBuffer.length);
                setLen(tl + newBuffer.length);
                if (mLength == BUFFER_LEN) {
                    if (recognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL) {
                        if (recognizeConfig.isWantRecognizeInManualMode()) {
                            recognizeInner(mLineNo);
                        }
                    }
                }
            } else {
                // 再放buffer就要满了，头部的要移走
                int left = tl + newBuffer.length - BUFFER_LEN;
                //MyLog.d(TAG, "left=" + left + " mLenth=" + mLength + " buffer.length:" + buffer.length + " BUFFER_LEN:" + BUFFER_LEN);
                // 往左移动 left 个位置
//            byte [] temp = new byte[BUFFER_LEN];

                System.arraycopy(mBuffer, left, mBuffer, 0, tl - left);
                System.arraycopy(newBuffer, 0, mBuffer, tl - left, newBuffer.length);
                setLen(BUFFER_LEN);
                if (recognizeConfig.getMode() == RecognizeConfig.MODE_AUTO && recognizeConfig.getAutoTimes() > 0) {
                    // 自动识别
                    recognizeInner(mLineNo);
                } else if (recognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL) {
                    if (recognizeConfig.isWantRecognizeInManualMode()) {
                        recognizeInner(mLineNo);
                    }
                }
            }
        }
    }

    int logtag = 0;

    public void setLen(int len) {
        if (++logtag % 500 == 0) {
            MyLog.d(TAG, "setLen" + " len=" + len);
        }
        mLength = len;
    }

    private void recognizeInner(final int lineNo) {
        tryInit();
        if (this.mClient != null) {
            if (mLength >= BUFFER_LEN) {
                if (!mProcessing) {
                    mProcessing = true;
                    int len = mLength;
                    final byte[] arr = new byte[len];
                    System.arraycopy(mBuffer, 0, arr, 0, len);
                    Observable.create(new ObservableOnSubscribe<Object>() {
                        @Override
                        public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                            // 识别次数打点
                            StatisticsAdapter.recordCountEvent("acr", "recognize", null);
                            long beginTs = System.currentTimeMillis();
                            RecognizeConfig recognizeConfig = ArcCloudManager.this.mRecognizeConfig;
                            if (recognizeConfig != null
                                    && recognizeConfig.getResultListener() != null
                                    && mLength >= BUFFER_LEN) {

                                mProcessing = true;
                                ACRCloudConfig.RecognizerType recType = ACRCloudConfig.RecognizerType.HUMMING;
                                HashMap hashMap = new HashMap();
                                String songName = recognizeConfig.getSongName();
                                if (!TextUtils.isEmpty(songName)) {
                                    int a = songName.indexOf("(");
                                    if (a != -1) {
                                        // 过滤掉歌曲
                                        songName = songName.substring(0, a);
                                    } else {
                                        a = songName.indexOf("（");
                                        if (a != -1) {
                                            songName = songName.substring(0, a);
                                        }
                                    }
                                    hashMap.put("title", songName);
                                }
                                String artist = recognizeConfig.getArtist();
                                if (!TextUtils.isEmpty(artist)) {
                                    hashMap.put("artist", artist);
                                }
                                MyLog.d(TAG, "开始识别 arr.length=" + arr.length + " mSampleRate=" + mSampleRate + " mChannels=" + mChannels);
                                String result = mClient.recognize(arr, arr.length, mSampleRate, mChannels, recType, hashMap);
                                MyLog.d(TAG, "识别结束");

                                if (DEBUG) {
                                    {
                                        File file = new File(U.getAppInfoUtils().getFilePathInSubDir("acr", "line" + mLineNo + ".pcm"));
                                        if (!file.getParentFile().exists()) {
                                            file.getParentFile().mkdirs();
                                        }
                                        BufferedSink bufferedSink = null;
                                        try {
                                            Sink sink = Okio.sink(file);
                                            bufferedSink = Okio.buffer(sink);
                                            bufferedSink.write(arr);
                                            MyLog.d(TAG, "写入文件 path:" + file.getAbsolutePath());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            if (null != bufferedSink) {
                                                bufferedSink.close();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    {
                                        File file = new File(U.getAppInfoUtils().getFilePathInSubDir("acr", "识别结果" + mLineNo + ".txt"));
                                        if (!file.getParentFile().exists()) {
                                            file.getParentFile().mkdirs();
                                        }
                                        BufferedSink bufferedSink = null;
                                        try {
                                            Sink sink = Okio.sink(file);
                                            bufferedSink = Okio.buffer(sink);
                                            bufferedSink.writeString(result, Charset.forName("UTF-8"));
                                            MyLog.d(TAG, "写入结果 path:" + file.getAbsolutePath());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            if (null != bufferedSink) {
                                                bufferedSink.close();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                mProcessing = false;
                                if (recognizeConfig != null && recognizeConfig.getMode() == RecognizeConfig.MODE_AUTO) {
                                    recognizeConfig.setAutoTimes(recognizeConfig.getAutoTimes() - 1);
                                    setLen(0);
                                }
                                if (recognizeConfig != null && recognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL) {
                                    recognizeConfig.setWantRecognizeInManualMode(false);
                                }
                                process(result, lineNo);
                            }
                            mProcessing = false;
                            long duration = System.currentTimeMillis() - beginTs;
                            // 打点统计acr的耗时
                            StatisticsAdapter.recordCalculateEvent("acr", "recognize_haoshi", duration, null);
                            emitter.onComplete();
                        }
                    }).subscribeOn(Schedulers.io())
                            .subscribe(new Observer<Object>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Object o) {

                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                } else {
                    //MyLog.d(TAG, "已经有任务在处理");
                }
            } else {
                MyLog.d(TAG, "buffer 不够");
            }
        } else {
            MyLog.d(TAG, "mClient==null");
        }
    }

    private void process(String result, int lineNo) {
        MyLog.d(TAG, "onResult" + " result=" + result);
        if (mRecognizeConfig != null) {
            RecognizeConfig recognizeConfig = mRecognizeConfig;
            JSONObject jsonObject = JSON.parseObject(result);
            JSONObject j1 = jsonObject.getJSONObject("metadata");
            if (j1 != null) {
                String text = j1.getString("humming");
                if (!TextUtils.isEmpty(text)) {
                    List<SongInfo> list = JSON.parseArray(text, SongInfo.class);
                    String songName = mRecognizeConfig.getSongName();
                    SongInfo targetSongInfo = null;
                    if (list != null && list.size() > 0) {
                        for (SongInfo songInfo : list) {
                            if (songInfo.getTitle().equalsIgnoreCase(songName)) {
                                targetSongInfo = songInfo;
                                // 识别成功打点
                                StatisticsAdapter.recordCountEvent("acr", "recognize_success", null);
                                break;
                            }
                        }
                    }
                    MyLog.d(TAG, " list=" + list + " targetSongInfo=" + targetSongInfo);
                    recognizeConfig.getResultListener().onResult(result, list, targetSongInfo, lineNo);
                }
            } else {
                recognizeConfig.getResultListener().onResult(result, null, null, lineNo);
            }
        }
    }

    public void recognizeInManualMode(int lineNo) {
        MyLog.d(TAG, "recognizeInManualMode");
        if (mRecognizeConfig != null && mRecognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL) {
            mLineNo = lineNo;
            mRecognizeConfig.setWantRecognizeInManualMode(true);
        }
    }

//    public void cancel() {
//        MyLog.d(TAG, "cancel");
//        if (mProcessing && this.mClient != null) {
//            mProcessing = false;
//            this.mClient.cancel();
//        }
//    }

    public void destroy() {
        MyLog.d(TAG, "destroy");

        if (this.mClient != null) {
            this.mClient.release();
            this.mInited = false;
            this.mClient = null;
        }
        stopRecognize();
    }
}
