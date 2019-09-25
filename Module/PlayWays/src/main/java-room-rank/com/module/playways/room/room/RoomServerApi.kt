package com.module.playways.room.room

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.PUT

public interface RoomServerApi {
    /**
     * "{\n\t\"gameID\" : 20000220,\n\t\"content\" : \"hello xxx\"\n}")
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/chat")
    abstract fun sendMsg(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 必传 "gameID": 0,"msgUrl": "string",
     * "receiver": [
     * {
     * "avatar": "string",
     * "description": "string",
     * "isSystem": true,
     * "mainLevel": 0,
     * "nickName": "string",
     * "sex": "SX_UNKNOWN",
     * "userID": 0
     * }
     * ]
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/audio-chat")
    abstract fun sendAudioMsg(@Body body: RequestBody): Observable<ApiResult>
}