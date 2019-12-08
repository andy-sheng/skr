package com.module.playways.party.room

import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

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

//
//    @Headers(ApiManager.ALWAYS_LOG_TAG)
//    @PUT("http://dev.game.inframe.mobi/v1/relaygame/give-up")
//    fun giveUpSing(@Body body: RequestBody): Call<ApiResult>
//

//

//
//    @GET("http://dev.game.inframe.mobi/v1/relaygame/sync-status")
//    fun syncStatus(@Query("roomID") roomID: Long): Call<ApiResult>
//
//    @PUT("http://dev.game.inframe.mobi/v1/relaygame/unlock-user-info")
//    fun sendUnlock(@Body body: RequestBody): Call<ApiResult>
//
//    @PUT("http://dev.game.inframe.mobi/v1/relaygame/mute")
//    fun mute(@Body body: RequestBody): Observable<ApiResult>
//
//    @GET("http://dev.game.inframe.mobi/v1/relaygame/timestamp")
//    fun timestamp(@Query("roomID") roomID: Int): Call<ApiResult>
//
//    /**
//     *  同意合唱/PK的结果 {"roomID": 0,"uniqTag": "string"}
//     */
//    @PUT("http://dev.game.inframe.mobi/v1/relaygame/agree-sing")
//    fun agreeSing(@Body body: RequestBody): Call<ApiResult>
//
//    /**
//     *  查询同意合唱/PK的结果 {"roomID": 0,"uniqTag": "string"}
//     */
//    @GET("http://dev.game.inframe.mobi/v1/relaygame/agree-sing-result")
//    fun getAgreeSingResult(@Query("roomID") roomID: Int, @Query("uniqTag") uniqTag: String): Call<ApiResult>
//
//    /**
//     *  获取房间结果信息
//     */
//    @GET("http://dev.game.inframe.mobi/v1/relaygame/result")
//    fun getRelayResult(@Query("roomID") roomID: Int): Call<ApiResult>
}