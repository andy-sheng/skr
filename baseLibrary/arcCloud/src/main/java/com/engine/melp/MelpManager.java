package com.engine.melp;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.engine.arccloud.RecognizeConfig;

import java.util.HashMap;

import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 每一句歌词上报一次
 */
public class MelpManager {

    public final static String TAG = "MelpManager";

    static final int BUFFER_LEN = 500 * 2048;  //足够大，必须要放下一句歌词

    byte[] mBuffer = new byte[BUFFER_LEN];

    int mLength = 0;
    int mSampleRate = -1;
    int mChannels = -1;
    int mLineNo = 0;
    long mBeginTs = 0;
    long mEndTs = 0;
    int uid = 0;

    RecognizeConfig mRecognizeConfig;

    public void startRecognize(RecognizeConfig recognizeConfig) {
        MyLog.d(TAG, "startCollect" + " recognizeConfig=" + recognizeConfig);
        this.mRecognizeConfig = recognizeConfig;
    }

    public void stopRecognize() {
        MyLog.d(TAG, "stopRecognize");
        this.mRecognizeConfig = null;
    }

    public void recognizeByMelp(int lineNo, long startTime, long endTime, int uid) {
        MyLog.d(TAG, "recognizeInManualMode");
        if (mRecognizeConfig != null) {
            this.uid = uid;
            mLineNo = lineNo;
            mBeginTs = startTime;
            mEndTs = endTime;
            mRecognizeConfig.setWantRecognizeInManualMode(true);
        }
    }

    /**
     * 利用 mBeginTs 和 mEndTs，判断ts是否落在该区域上
     *
     * @param buffer
     * @param sampleRate
     * @param nChannels
     * @param ts         作为此buffer的时间戳
     */
    public void putPool(byte[] buffer, int sampleRate, int nChannels, long ts) {
        MyLog.d(TAG, "putPool" + " buffer=" + buffer + " sampleRate=" + sampleRate + " nChannels=" + nChannels + " ts=" + ts);
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

        if (mSampleRate < 0) {
            mSampleRate = sampleRate;
        }
        if (mChannels < 0) {
            mChannels = nChannels;
        }

        // TODO: 2019/3/11 很多ts为0的数据，不知道是否舍弃
        if (ts < mBeginTs) {
            // 舍弃这部分数据
            MyLog.w(TAG, "时间戳比歌词开始时间都短，舍弃这部分数据");
        } else if (ts >= mEndTs) {
            // 此时上传处理数据
            System.arraycopy(newBuffer, 0, mBuffer, mLength, newBuffer.length);
            recognizeInner();
        } else {
            // 此部分数据加到缓存上
            System.arraycopy(newBuffer, 0, mBuffer, mLength, newBuffer.length);
            mLength = mLength + newBuffer.length;
        }
    }

    private void recognizeInner() {
        int len = mLength;
        final byte[] arr = new byte[len];
        System.arraycopy(mBuffer, 0, arr, 0, len);

        // 识别
        MelpScoreServerApi melpScoreServerApi = ApiManager.getInstance().createService(MelpScoreServerApi.class);

        HashMap<String, Object> map = new HashMap<>();
        map.put("BeginTs", mBeginTs);
        map.put("Channels", mChannels);
        map.put("Data", U.getBase64Utils().encode(arr));
        map.put("DataType", "pcm");
        map.put("EndTs", mEndTs);
        map.put("Len", arr.length);
        map.put("SampleRate", mSampleRate);
        map.put("SongID", mRecognizeConfig.getSongId());
        map.put("Type", 1);
        map.put("UserID", uid);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        melpScoreServerApi.requestMelpScore(body)
                .subscribe(new Consumer<ApiResult>() {
                    @Override
                    public void accept(ApiResult result) throws Exception {
                        if (result.getErrno() == 0) {
                            MyLog.d(TAG, "Sucess");
                            if (mRecognizeConfig.getMelpRecognizeListener() != null) {
                                mRecognizeConfig.getMelpRecognizeListener().onResult("成功了哈哈");
                            }
                        } else {
                            MyLog.d(TAG, "Failed");
                        }
                    }
                });

    }

}

