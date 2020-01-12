package com.engine.api;


import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface EngineServerApi {

    @GET("http://dev.api.inframe.mobi/v1/messenger/agora-token")
    Call<ApiResult> getToken(@Query("channelName") String channelName);


    @Headers(ApiManager.NO_NEED_LOGIN_TAG)
    @GET("http://dev.kconf.inframe.mobi/v1/kconf/android-audio-cfg")
    Call<ApiResult> getAudioConfig(@Query("manufacturer") String manufacturer,@Query("OSVersion") String OSVersion);

    @GET("http://dev.kconf.inframe.mobi/v1/kconf/cdn-cfg")
    Observable<ApiResult> getCdnCfg(@Query("userID") int userID);
}
