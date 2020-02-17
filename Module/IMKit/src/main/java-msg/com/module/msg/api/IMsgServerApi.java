package com.module.msg.api;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface IMsgServerApi {

    @GET("http://dev.api.inframe.mobi/v1/messenger/check-send-msg")
    Observable<ApiResult> checkSendMsg(@Query("toUserID") int toUserID);

    @PUT("http://dev.api.inframe.mobi/v1/messenger/incr-send-msg-times")
    Observable<ApiResult> incSendMsgTimes(@Body RequestBody requestBody);

    @PUT("http://dev.api.inframe.mobi/v1/mall/relation-apply-response")
    Observable<ApiResult> relationApplyResponse(@Body RequestBody requestBody);

    @PUT("http://dev.api.inframe.mobi/v1/club/refuse-invitation")
    Observable<ApiResult> clubRefuse(@Body RequestBody requestBody);


    @PUT("http://dev.api.inframe.mobi/v1/club/accept-invitation")
    Observable<ApiResult> clubAgree(@Body RequestBody requestBody);

}
