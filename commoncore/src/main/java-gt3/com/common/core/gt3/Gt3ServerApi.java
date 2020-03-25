package com.common.core.gt3;

import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Gt3ServerApi {

    // 返回code
    @Headers(ApiManager.NO_NEED_LOGIN_TAG)
    @GET("http://dev.api.inframe.mobi/v1/geetest/on-init")
    Call<ApiResult> api1(@Query("uuid") String phoneNum);

    // 保存code
//    @Headers(ApiManager.NO_NEED_LOGIN_TAG)
//    @GET("http://dev.api.inframe.mobi/v1/passport/get-code-for-login")
//    Call<ApiResult> getLoginCode(@Query("phone") String phoneNum,
//                                 @Query("challenge") String challenge,
//                                 @Query("validate") String validate,
//                                 @Query("seccode") String seccode);


}
