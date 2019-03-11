package com.engine.melp;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MelpScoreServerApi {

    @POST("http://dev.grade.inframe.mobi/v1/grade/sings")
    Observable<ApiResult> requestMelpScore(@Body RequestBody body);
}
