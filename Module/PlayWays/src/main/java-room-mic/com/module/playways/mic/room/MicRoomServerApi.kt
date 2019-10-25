package com.module.playways.mic.room

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface MicRoomServerApi {

    @GET("http://dev.game.inframe.mobi/v2/raceroom/check-rank")
    abstract fun checkRank(@Query("roomType") roomType: Int): Call<ApiResult>

    /**
     * 创建房间
     * {
     *      "levelLimit": "RLL_All",
     *      "roomName": "string"
     * }
     */
    @PUT("http://dev.game.inframe.mobi/v1/microom/create-room")
    fun createRoom(@Body body: RequestBody): Call<ApiResult>

    /**
     * 拉取房间可邀请的段位
     */
    @GET("http://dev.game.inframe.mobi/v1/microom/roomtype-permission-list")
    fun getRoomPermmissionList(): Call<ApiResult>
}