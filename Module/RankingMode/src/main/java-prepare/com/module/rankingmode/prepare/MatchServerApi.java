package com.module.rankingmode.prepare;


import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * 匹配过程中向服务器请求api
 */
public interface MatchServerApi {

    @PUT("http://dev.game.inframe.mobi/v1/game/query-match")
    Observable<ApiResult> startMatch(@Body RequestBody body);


    @PUT("http://dev.game.inframe.mobi/v1/game/cancel-match")
    Observable<ApiResult> cancleMatch(@Body RequestBody body);


    @PUT("http://dev.game.inframe.mobi/v1/game/join")
    Observable<ApiResult> joinGame(@Body RequestBody body);

    @GET("http://dev.game.inframe.mobi/v1/game/join")
    Observable<ApiResult> getCurrentGameDate(@Query("gameID") int gameID);

    @PUT("http://dev.game.inframe.mobi/v1/game/ready")
    Observable<ApiResult> readyGame(@Body RequestBody body);

    @GET("http://dev.game.inframe.mobi/v1/game/ready")
    Observable<ApiResult> getCurrentReadyData(@Query("gameID") int gameID);

}
