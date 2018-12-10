package com.module.rankingmode.prepare;


import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * 匹配过程中向服务器请求api
 */
public interface MatchServerApi {

    @Headers("Content-Type : application/json")
    @PUT("http://dev.game.inframe.mobi/v1/game/query-match")
    Observable<ApiResult> startMatch(@Body RequestBody body);


    @Headers("Content-Type : application/json")
    @PUT("http://dev.game.inframe.mobi/v1/game/cancel-match")
    Observable<ApiResult> cancleMatch(@Body RequestBody body);


    @PUT("http://dev.game.inframe.mobi/v1/game/join")
    Observable<ApiResult> joinRoom(@Body RequestBody body);

    @GET("http://dev.game.inframe.mobi/v1/game/join")
    Observable<ApiResult> getCurrentGameDate(@Query("gameID") int gameID);

}
