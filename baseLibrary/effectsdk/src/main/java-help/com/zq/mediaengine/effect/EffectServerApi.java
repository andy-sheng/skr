package com.zq.mediaengine.effect;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface EffectServerApi {

    @GET("dev.kconf.inframe.mobi/v1/kconf/beauty-service-ca")
    Observable<ApiResult> getDyLicenseUrl(@Query("platform") int platform);
}
