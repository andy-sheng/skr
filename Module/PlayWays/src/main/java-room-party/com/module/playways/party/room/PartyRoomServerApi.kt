package com.module.playways.party.room

import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT

interface PartyRoomServerApi {
    /**
     * 拉取房间可邀请的段位
     */
    @GET("http://dev.game.inframe.mobi/v1/microom/roomtype-permission-list")
    fun getRoomPermmissionList(): Call<ApiResult>


    /**
     * 创建房间
     */
    @PUT("http://dev.game.inframe.mobi/v1/partyroom/create-room")
    fun createRoom(@Body body: RequestBody): Call<ApiResult>

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/partyroom/exit-room")
    fun exitRoom(@Body body: RequestBody): Call<ApiResult>

    @PUT("http://dev.game.inframe.mobi/v1/partygame/heartbeat")
    fun heartbeat(@Body body: RequestBody): Call<ApiResult>

    /**
     * {
    "adminUserID": 0,
    "roomID": 0,
    "setType": "SAT_UNKNOWN"  SAT_UNKNOWN, SAT_ADD, SAT_DEL
    }
     */
    @PUT("http://dev.game.inframe.mobi/v1/partygame/set-admin")
    fun setAdmin(@Body body: RequestBody): Call<ApiResult>
}