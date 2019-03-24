package com.common.core.kouling.api;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface KouLingServerApi {

    // 返回code
    @GET("http://dev.api.inframe.mobi/v1/event/get-code")
    Observable<ApiResult> getCodeByToken(@Query("token")String token);

    // 保存code
    @GET("http://dev.api.inframe.mobi/v1/event/set-code")
    Observable<ApiResult> setTokenByCode(@Query("code")String code);

}
