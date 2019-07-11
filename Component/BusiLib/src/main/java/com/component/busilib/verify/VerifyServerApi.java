package com.component.busilib.verify;

import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface VerifyServerApi {

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @GET("http://dev.room.inframe.mobi/v1/room/vedio-permission")
    Observable<ApiResult> checkJoinVideoRoomPermission();

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @GET("http://dev.game.inframe.mobi/v1/magpie/check-auth")
    Observable<ApiResult> checkJoinDoubleRoomPermission();

    @GET("http://dev.room.inframe.mobi/v1/room/get-playbook-permission")
    Observable<ApiResult> checkJoinAudioRoomPermission(@Query("tagID") int tagId);
}
