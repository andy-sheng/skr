package com.module.playways.mic.room

import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

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


    @PUT("http://dev.stand.inframe.mobi/v1/micgame/round-over")
    fun sendRoundOver(@Body body: RequestBody): Call<ApiResult>

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.stand.inframe.mobi/v1/micgame/give-up")
    fun giveUpSing(@Body body: RequestBody): Call<ApiResult>

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.stand.inframe.mobi/v1/microom/req-kick-user")
    fun reqKickUser(@Body body: RequestBody): Call<ApiResult>

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.room.inframe.mobi/v1/microom/exit-room")
    fun exitRoom(@Body body: RequestBody): Call<ApiResult>

    @GET("http://dev.game.inframe.mobi/v1/micgame/sync-status")
    fun syncStatus(@Query("roomID") roomID: Long): Call<ApiResult>
    
    /**
     * 拉取房间可邀请的段位
     */
    @GET("http://dev.game.inframe.mobi/v1/micgame/user-list")
    fun getMicSeatUserList(@Query("userID") userID: Int,
                           @Query("roomID") roomID: Int): Call<ApiResult>
}