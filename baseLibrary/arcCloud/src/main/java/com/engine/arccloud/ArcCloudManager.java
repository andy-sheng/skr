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
import io.reactivex.schedulers.Schedulers;

public class ArcCloudManager implements IACRCloudListener {
    public final static String TAG = "ArcCloudManager";

    static final int BUFFER_LEN = 44100 * 4 * 10;

    byte[] mBuffer = new byte[BUFFER_LEN];
    int mLenth = 0;
    int mSampleRate = -1;
    int mChannels = -1;

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
        MyLog.d(TAG, "tryInit");
        if (mClient == null) {
            synchronized (this) {
                if (mClient == null) {
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
        MyLog.d(TAG, "stopCollect");
        // 停止积攒
        mLenth = 0;
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
        if (mLenth + buffer.length <= BUFFER_LEN) {
            // buffer还没满，足够容纳，那就放呗
            System.arraycopy(buffer, 0, mBuffer, mLenth, buffer.length);
            mLenth += buffer.length;
            if (mRecognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL && mLenth > BUFFER_LEN / 2) {
                if (mRecognizeConfig.isWantRecognizeInManualMode()) {
                    recognizeInner();
                }
            }
        } else {
            // 再放buffer就要满了，头部的要移走
            int left = mLenth + buffer.length - BUFFER_LEN;
            // 往左移动 left 个位置
            System.arraycopy(mBuffer, left, mBuffer, 0, mLenth - left);
            mLenth = mLenth - left;
            System.arraycopy(buffer, 0, mBuffer, mLenth, buffer.length);
            mLenth += buffer.length;
            if (mRecognizeConfig.getMode() == RecognizeConfig.MODE_AUTO) {
                // 自动识别
                recognizeInner();
            } else if (mRecognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL) {
                if (mRecognizeConfig.isWantRecognizeInManualMode()) {
                    recognizeInner();
                }
            }
        }
    }

    private void recognizeInner() {
        tryInit();
        if (this.mClient != null) {
            if (mLenth > BUFFER_LEN / 2 && !mProcessing) {
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        if (mRecognizeConfig != null && mRecognizeConfig.getResultListener() != null) {
                            mProcessing = true;
                            ACRCloudConfig.RecognizerType recType = ACRCloudConfig.RecognizerType.HUMMING;
                            HashMap hashMap = new HashMap();
                            String songName = mRecognizeConfig.getSongName();
                            if (!TextUtils.isEmpty(songName)) {
                                hashMap.put("title", songName);
                            }
                            String artist = mRecognizeConfig.getArtist();
                            if (!TextUtils.isEmpty(artist)) {
                                hashMap.put("artist", artist);
                            }
                            String result = mClient.recognize(mBuffer, mLenth, mSampleRate, mChannels, recType, hashMap);
                            mLenth = 0;
                            mProcessing = false;
                            if (mRecognizeConfig.getMode() == RecognizeConfig.MODE_AUTO) {
                                mRecognizeConfig.setAutoTimes(mRecognizeConfig.getAutoTimes() - 1);
                            }
                            if (mRecognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL) {
                                mRecognizeConfig.setWantRecognizeInManualMode(false);
                            }
                            process(result);

                        }
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.io())
                        .subscribe();
            }
        }
    }

    private void process(String result) {
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
                    MyLog.d(TAG, "onResult" + " result=" + result);
                    MyLog.d(TAG, " list=" + list + " targetSongInfo=" + targetSongInfo);

                    mRecognizeConfig.getResultListener().onResult(result, list, targetSongInfo);
                }
            }
        }
    }

    public void recognizeInManualMode() {
        if (mRecognizeConfig.getMode() == RecognizeConfig.MODE_MANUAL) {
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
