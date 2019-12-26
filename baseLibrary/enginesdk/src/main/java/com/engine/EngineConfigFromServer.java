package com.engine;

import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.engine.api.EngineServerApi;

import java.io.Serializable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 这些值只代表服务器建议的值
 * 不修改，持久化
 * enableAudioPreview = true 只代表服务端的配置建议开启耳返，不代表耳返是否开启
 */
public class EngineConfigFromServer implements Serializable {

    public boolean hasServerConfig = false; // 是否有服务端配置

    @JSONField(name = "audioMixLatencyHeadSet")
    int accMixingLatencyOnHeadset = 0; //混音延迟（耳机），单位毫秒，-1表示未知

    @JSONField(name = "audioMixLatencySpeaker")
    int accMixingLatencyOnSpeaker = 0; //混音延迟（外放），单位毫秒，-1表示未知

    @JSONField(name = "audioPreview")
    boolean enableAudioPreview = false; //是否开启耳返 只代表

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

    public boolean isEnableAudioPreview() {
        return enableAudioPreview;
    }

    public void setEnableAudioPreview(boolean enableAudioPreview) {
        this.enableAudioPreview = enableAudioPreview;
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
        return "hasServerConfig=" + hasServerConfig +
                "\n accMixingLatencyOnHeadset=" + accMixingLatencyOnHeadset +
                "\n accMixingLatencyOnSpeaker=" + accMixingLatencyOnSpeaker +
                "\n enableAudioPreview=" + enableAudioPreview +
                "\n useExternalAudio=" + useExternalAudio +
                "\n enableAudioLowLatency=" + enableAudioLowLatency;
    }

    // 获得一个服务配置的config
    public static EngineConfigFromServer getDefault() {
        //只存服务器下发的原始数据
        EngineConfigFromServer configFromServer = null;
        long lastTs = U.getPreferenceUtils().getSettingLong(U.getPreferenceUtils().longlySp(), "EngineConfigFromServerUpdateTs", 0);
        MyLog.i("Params", "configFromServer lastTs=" + lastTs);
        if (System.currentTimeMillis() - lastTs > 24 * 3600 * 1000) {
            // 请求服务器
            if (Looper.getMainLooper() == Looper.myLooper()) {
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        getFromServerInner("async");
                        emitter.onComplete();
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe();
            } else {
                configFromServer = getFromServerInner("sync");
            }
        } else {
            String json = U.getPreferenceUtils().getSettingString(U.getPreferenceUtils().longlySp(), "EngineConfigFromServer", "");
            if (!TextUtils.isEmpty(json)) {
                configFromServer = JSON.parseObject(json, EngineConfigFromServer.class);
            }
        }
        if (configFromServer == null) {
            configFromServer = new EngineConfigFromServer();
        }
        MyLog.i("Params", configFromServer.toString());
        return configFromServer;
    }

    private static EngineConfigFromServer getFromServerInner(String from) {
        MyLog.d("Params", "getFromServerInner from=" + from);
        EngineServerApi api = ApiManager.getInstance().createService(EngineServerApi.class);
        Call<ApiResult> apiResultCall = api.getAudioConfig(U.getDeviceUtils().getProductModel()
                , String.valueOf(android.os.Build.VERSION.SDK_INT));
        if (apiResultCall != null) {
            try {
                Response<ApiResult> resultResponse = apiResultCall.execute();
                if (resultResponse != null) {
                    ApiResult obj = resultResponse.body();
                    if (obj != null) {
                        MyLog.d("Params", "getFromServerInner obj=" + obj);
                        // 请求成功
                        if (obj.getErrno() == 0) {
                            MyLog.d("Params", "getFromServerInner json=" + obj.getData().toString());
                            EngineConfigFromServer configFromServer = JSON.parseObject(obj.getData().toString(), EngineConfigFromServer.class);
                            configFromServer.hasServerConfig = true;
                            // 持久化
                            U.getPreferenceUtils().setSettingString(U.getPreferenceUtils().longlySp(), "EngineConfigFromServer", JSON.toJSONString(configFromServer));
                            U.getPreferenceUtils().setSettingLong(U.getPreferenceUtils().longlySp(), "EngineConfigFromServerUpdateTs", System.currentTimeMillis());
                            return configFromServer;
                        } else if (obj.getErrno() == 102) {
                            return null;
                        } else if (obj.getErrno() == 1) {
                            // 该机型没有配置，也先算了
                            U.getPreferenceUtils().setSettingString(U.getPreferenceUtils().longlySp(), "EngineConfigFromServer", "");
                            U.getPreferenceUtils().setSettingLong(U.getPreferenceUtils().longlySp(), "EngineConfigFromServerUpdateTs", System.currentTimeMillis());
                        }
                    } else {
                    }
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return null;
    }

    /**
     * 0 是用户未设置
     * 1 是用户设置使用自采集
     * 2 是用户设置关闭自采集
     *
     * @return
     */
    public static int getSelfCollectionSwitch() {
        int r = U.getPreferenceUtils().getSettingInt("engine_self_collection", 0);
        return r;
    }


    public static void setSelfCollectionSwitch(int r) {
        U.getPreferenceUtils().setSettingInt("engine_self_collection", r);
    }
}
