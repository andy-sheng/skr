package com.component.mediaengine.effect;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EffectServerApi {

    @GET("http://dev.kconf.inframe.mobi/v1/kconf/beauty-service-ca")
    Observable<ApiResult> getDyLicenseUrl(@Query("platform") int platform);
}
