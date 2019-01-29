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
import com.common.utils.U;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ArcCloudManager implements IACRCloudListener {
    public final static String TAG = "ArcCloudManager";

    static final int BUFFER_LEN = 650 * 2048;

    byte[] mBuffer = new byte[BUFFER_LEN];
    int mLength = 0;
    int mSampleRate = -1;
    int mChannels = -1;
    int mLineNo = 0;
    RecognizeConfig mRecognizeConfig;

    @Override
    public void onResult(ACRCloudResult acrCloudResult) {
        MyLog.d(TAG, "onResult" + " acrCloudResult=" + acrCloudResult.getResult());
    }

    @Override
    public void onVolumeChanged(double v) {

    }

    private static class ArcCloudManagerHolder {
        private static final ArcCloudManager INSTANCE = new ArcCloudManager();
    }

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
        this.mRecognizeConfig = recognizeConfig;
    }

    public void stopRecognize() {
        MyLog.d(TAG,"stopRecognize" );
        // 停止积攒
        setLen(0);
        this.mRecognizeConfig = null;
    }

    public void putPool(byte[] buffer, int sampleRate, int nChannels) {
        if (mRecognizeConfig == null) {
            return;
        }
        if (mRecognizeConfig.mode == RecognizeConfig.MODE_AUTO) {
            if (mRecognizeConfig.getAutoTimes() <= 0) {
                return;
            }
        }

        if (mSampleRate < 0) {
            mSampleRate = sampleRate;
        }
        if (mChannels < 0) {
            mChannels = nChannels;
        }
        if (mLength + buffer.length <= BUFFER_LEN) {
            // buffer还没满，足够容纳，那就放呗
            System.arraycopy(buffer, 0, mBuffer, mLength, buffer.length);
            setLen(mLength + buffer.length);
            if (mRecognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL && mLength == BUFFER_LEN) {
                if (mRecognizeConfig.isWantRecognizeInManualMode()) {
                    recognizeInner(mLineNo);
                }
            }
        } else {
            // 再放buffer就要满了，头部的要移走
            int left = mLength + buffer.length - BUFFER_LEN;
            //MyLog.d(TAG, "left=" + left + " mLenth=" + mLength + " buffer.length:" + buffer.length + " BUFFER_LEN:" + BUFFER_LEN);
            // 往左移动 left 个位置
//            byte [] temp = new byte[BUFFER_LEN];

            System.arraycopy(mBuffer, left, mBuffer, 0, mLength - left);
            System.arraycopy(buffer, 0, mBuffer, mLength - left, buffer.length);
            setLen(BUFFER_LEN);
            if (mRecognizeConfig.getMode() == RecognizeConfig.MODE_AUTO) {
                // 自动识别
                recognizeInner(mLineNo);
            } else if (mRecognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL) {
                if (mRecognizeConfig.isWantRecognizeInManualMode()) {
                    recognizeInner(mLineNo);
                }
            }
        }
    }

    int logtag = 0;

    public synchronized void setLen(int len) {
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
                    Observable.create(new ObservableOnSubscribe<Object>() {
                        @Override
                        public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                            RecognizeConfig recognizeConfig = ArcCloudManager.this.mRecognizeConfig;
                            if (recognizeConfig != null
                                    && recognizeConfig.getResultListener() != null
                                    && mLength >= BUFFER_LEN) {

                                mProcessing = true;
                                MyLog.d(TAG, "开始识别");
                                ACRCloudConfig.RecognizerType recType = ACRCloudConfig.RecognizerType.HUMMING;
                                HashMap hashMap = new HashMap();
                                String songName = recognizeConfig.getSongName();
                                if (!TextUtils.isEmpty(songName)) {
                                    hashMap.put("title", songName);
                                }
                                String artist = recognizeConfig.getArtist();
                                if (!TextUtils.isEmpty(artist)) {
                                    hashMap.put("artist", artist);
                                }
                                int len = mLength;
                                byte[] arr = new byte[len];
                                System.arraycopy(mBuffer, 0, arr, 0, len);
//                        setLen(0);
                                MyLog.d(TAG, "len:" + len);
                                String result = mClient.recognize(arr, len, mSampleRate, mChannels, recType, hashMap);
                                MyLog.d(TAG, "识别结束");
                                mProcessing = false;
                                if (recognizeConfig!=null && recognizeConfig.getMode() == RecognizeConfig.MODE_AUTO) {
                                    recognizeConfig.setAutoTimes(recognizeConfig.getAutoTimes() - 1);
                                }
                                if (recognizeConfig!=null && recognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL) {
                                    recognizeConfig.setWantRecognizeInManualMode(false);
                                }
                                process(result,lineNo);
                            }
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
                    MyLog.d(TAG, "已经有任务在处理");
                }
            } else {
                MyLog.d(TAG, "buffer 不够");
            }
        } else {
            MyLog.d(TAG, "mClient==null");
        }
    }

    private void process(String result,int lineNo) {
        MyLog.d(TAG, "onResult" + " result=" + result);
        if (mRecognizeConfig != null) {
            JSONObject jsonObject = JSON.parseObject(result);
            JSONObject j1 = jsonObject.getJSONObject("metadata");
            if (j1 != null) {
                String text = j1.getString("humming");
                if (!TextUtils.isEmpty(text)) {
                    List<SongInfo> list = JSON.parseArray(text, SongInfo.class);
                    String songName = mRecognizeConfig.getSongName();
                    SongInfo targetSongInfo = null;
                    for (SongInfo songInfo : list) {
                        if (songInfo.getTitle().equalsIgnoreCase(songName)) {
                            targetSongInfo = songInfo;
                            break;
                        }
                    }
                    MyLog.d(TAG, " list=" + list + " targetSongInfo=" + targetSongInfo);
                    mRecognizeConfig.getResultListener().onResult(result, list, targetSongInfo,lineNo);
                }
            } else {
                mRecognizeConfig.getResultListener().onResult(result, null, null,lineNo);
            }
        }
    }

    public void recognizeInManualMode(int lineNo) {
        MyLog.d(TAG,"recognizeInManualMode" );
        if (mRecognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL) {
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
