package com.common.core.gt3;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Gt3ServerApi {

    // 返回code
    @GET("http://dev.api.inframe.mobi/v1/event/get-code")
    Call<String> api1(@Query("token") String token);

    // 保存code
    @POST("http://dev.api.inframe.mobi/v1/event/set-token")
    Call<String> api2(@Query("code") String code);

    //领取
    @POST("http://dev.api.inframe.mobi/v1/event/app-home-signin")
    Call<String> signIn(@Body RequestBody body);

}
