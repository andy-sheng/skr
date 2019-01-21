package com.engine.arccloud;

import android.content.Context;
import android.util.Log;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.utils.U;

public class ArcCloudManager implements IACRCloudListener {
    public final static String TAG = "ArcCloudManager";

    @Override
    public void onResult(String result) {
        U.getToastUtil().showShort(result);
        MyLog.d(TAG, "onResult" + " result=" + result);
        JSONObject jsonObject = JSON.parseObject(result);
        MyLog.d(TAG, "onResult" + " jsonObject=" + jsonObject);
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

    private ArcCloudManager() {

    }

    public static final ArcCloudManager getInstance() {
        return ArcCloudManagerHolder.INSTANCE;
    }

    void tryInit() {
        MyLog.d(TAG, "tryInit");

        if (mClient == null) {
            synchronized (this) {
                if (mClient == null) {
                    ACRCloudConfig mConfig = new ACRCloudConfig();
                    mConfig.acrcloudListener = this;

                    // If you implement IACRCloudResultWithAudioListener and override "onResult(ACRCloudResult result)", you can get the Audio data.
                    //mConfig.acrcloudResultWithAudioListener = this;

                    mConfig.context = U.app();

                    mConfig.host = "identify-cn-north-1.acrcloud.com";
//        mConfig.dbPath = path; // offline db path, you can change it with other path which this app can access.
                    mConfig.accessKey = "54b5d9b267c9f900c54dde6e6e86ebe4";
                    mConfig.accessSecret = "QsE01a4rjRr7DwRNsxSkyQL2U7EIfhq2pOFhJWPh";
                    mConfig.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTP; // PROTOCOL_HTTPS
                    mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;
                    //mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_LOCAL;
                    //mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_BOTH;

                    this.mClient = new ACRCloudClient();
                    // If reqMode is REC_MODE_LOCAL or REC_MODE_BOTH,
                    // the function initWithConfig is used to load offline db, and it may cost long time.
                    mInited = this.mClient.initWithConfig(mConfig);
                }
            }
        }
    }

    public void startRecognize() {
        tryInit();
        if (!this.mInited) {
            MyLog.d(TAG, "start");
            return;
        }
        if (!mProcessing) {
            mProcessing = true;
            if (this.mClient == null || !this.mClient.startRecognize()) {
                mProcessing = false;
            }
            mLastStartTs = System.currentTimeMillis();
        }
    }

    public void stopRecognize() {
        MyLog.d(TAG, "stopRecognize");

        if (mProcessing && this.mClient != null) {
            this.mClient.stopRecordToRecognize();
        }
        mProcessing = false;
    }

    public String recognize(byte[] buffer, int bufferLen, int sampleRate, int nChannels) {
        if (this.mClient != null) {
            ACRCloudConfig.ACRCloudRecognizeType recType = ACRCloudConfig.ACRCloudRecognizeType.ACR_OPT_REC_HUMMING;
            return this.mClient.recognize(buffer, bufferLen, sampleRate, nChannels, recType);
        }
        return "";
    }

    public void cancel() {
        MyLog.d(TAG, "cancel");

        if (mProcessing && this.mClient != null) {
            mProcessing = false;
            this.mClient.cancel();
        }
    }

    public void destroy() {
        MyLog.d(TAG, "destroy");

        if (this.mClient != null) {
            this.mClient.release();
            this.mInited = false;
            this.mClient = null;
        }
    }
}
