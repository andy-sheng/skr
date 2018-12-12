package com.module.rankingmode.room;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.PUT;

public interface RoomServerApi {
    /**
     * "{\n\t\"gameID\" : 20000220,\n\t\"content\" : \"hello xxx\"\n}")
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/chat")
    Observable<ApiResult> sendMsg(@Body RequestBody body);

}
