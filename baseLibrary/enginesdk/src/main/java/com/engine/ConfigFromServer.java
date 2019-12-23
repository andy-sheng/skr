package com.engine;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.engine.api.EngineServerApi;

import java.io.Serializable;

import retrofit2.Call;
import retrofit2.Response;

public class ConfigFromServer implements Serializable {
    @JSONField(name = "audioMixLatencyHeadSet")
    int accMixingLatencyOnHeadset = 0; //混音延迟（耳机），单位毫秒，-1表示未知

    @JSONField(name = "audioMixLatencySpeaker")
    int accMixingLatencyOnSpeaker = 0; //混音延迟（外放），单位毫秒，-1表示未知

    @JSONField(name = "audioPreview")
    boolean enableAudioPreviewLatencyTest = false; //是否开启耳返

    @JSONField(name = "externalAudio")
    boolean useExternalAudio = false;//是否开启自采集

    @JSONField(name = "openSL")
    boolean enableAudioLowLatency = false;//  是否开启openSL采集

    public int getAccMixingLatencyOnHeadset() {
        return accMixingLatencyOnHeadset;
    }

    public void setAccMixingLatencyOnHeadset(int accMixingLatencyOnHeadset) {
        this.accMixingLatencyOnHeadset = accMixingLatencyOnHeadset;
    }

    public int getAccMixingLatencyOnSpeaker() {
        return accMixingLatencyOnSpeaker;
    }

    public void setAccMixingLatencyOnSpeaker(int accMixingLatencyOnSpeaker) {
        this.accMixingLatencyOnSpeaker = accMixingLatencyOnSpeaker;
    }

    public boolean isEnableAudioPreviewLatencyTest() {
        return enableAudioPreviewLatencyTest;
    }

    public void setEnableAudioPreviewLatencyTest(boolean enableAudioPreviewLatencyTest) {
        this.enableAudioPreviewLatencyTest = enableAudioPreviewLatencyTest;
    }

    public boolean isUseExternalAudio() {
        return useExternalAudio;
    }

    public void setUseExternalAudio(boolean useExternalAudio) {
        this.useExternalAudio = useExternalAudio;
    }

    public boolean isEnableAudioLowLatency() {
        return enableAudioLowLatency;
    }

    public void setEnableAudioLowLatency(boolean enableAudioLowLatency) {
        this.enableAudioLowLatency = enableAudioLowLatency;
    }

    @Override
    public String toString() {
        return "ConfigFromServer{" +
                "accMixingLatencyOnHeadset=" + accMixingLatencyOnHeadset +
                ", accMixingLatencyOnSpeaker=" + accMixingLatencyOnSpeaker +
                ", enableAudioPreviewLatencyTest=" + enableAudioPreviewLatencyTest +
                ", useExternalAudio=" + useExternalAudio +
                ", enableAudioLowLatency=" + enableAudioLowLatency +
                '}';
    }

    // 获得一个服务配置的config
    public static ConfigFromServer getDefault() {
        //只存服务器下发的原始数据
        ConfigFromServer configFromServer = null;
        long lastTs = U.getPreferenceUtils().getSettingLong(U.getPreferenceUtils().longlySp(), "EngineConfigFromServerUpdateTs", 0);
        if (System.currentTimeMillis() - lastTs > 24 * 3600 * 1000) {
            // 请求服务器
            EngineServerApi api = ApiManager.getInstance().createService(EngineServerApi.class);
            if (api != null) {
                Call<ApiResult> apiResultCall = api.getAudioConfig(U.getDeviceUtils().getProductModel()
                        , String.valueOf(android.os.Build.VERSION.SDK_INT));
                if (apiResultCall != null) {
                    try {
                        Response<ApiResult> resultResponse = apiResultCall.execute();
                        if (resultResponse != null) {
                            ApiResult obj = resultResponse.body();
                            if (obj != null) {
                                // 请求成功
                                if (obj.getErrno() == 0) {
                                    configFromServer = JSON.parseObject(obj.getData().toString(), ConfigFromServer.class);
                                    // 持久化
                                    U.getPreferenceUtils().setSettingString(U.getPreferenceUtils().longlySp(), "EngineConfigFromServer", obj.getData().toString());

                                } else {

                                }
                                // 记录时间戳
                                U.getPreferenceUtils().setSettingLong(U.getPreferenceUtils().longlySp(), "EngineConfigFromServerUpdateTs", System.currentTimeMillis());
                            } else {
                            }
                        }
                    } catch (Exception e) {
                        MyLog.e(e);
                    }
                }
            }
        } else {
            String json = U.getPreferenceUtils().getSettingString(U.getPreferenceUtils().longlySp(), "EngineConfigFromServer", "");
            if (!TextUtils.isEmpty(json)) {
                configFromServer = JSON.parseObject(json, ConfigFromServer.class);
            }
        }
        if (configFromServer == null) {
            configFromServer = new ConfigFromServer();
        }
        MyLog.i("Params",configFromServer.toString());
        return configFromServer;
    }

    public void save2Pref() {
        U.getPreferenceUtils().setSettingString(U.getPreferenceUtils().longlySp(), "EngineConfigFromServer", JSON.toJSONString(this));
        U.getPreferenceUtils().setSettingLong(U.getPreferenceUtils().longlySp(), "EngineConfigFromServerUpdateTs", Long.MAX_VALUE);
    }
}
