package com.common.core.myinfo;


import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.PUT;

public interface MyUserInfoServerApi {
    @Headers("Content-Type : application/json")
    @PUT("v1/uprofile/information")
    Observable<ApiResult> updateInfo(@Body RequestBody body);
}
