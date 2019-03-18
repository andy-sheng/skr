package com.engine.token;

import com.common.rxretrofit.ApiResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AgoraTokenApi {

    @GET("http://dev.game.inframe.mobi/v1/messenger/agora-token")
    Call<ApiResult> getToken(@Query("channelName") String channelName);
}
