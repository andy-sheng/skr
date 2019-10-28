package com.module.playways.mic.room

import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface MicRoomServerApi {

    @GET("v1/fuel/mic-room-list")
    fun getMicHomeRoomList(@Query("offset") offset: Int, @Query("testList") testList: String, @Query("vars") vars: String): Call<ApiResult>

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


    @PUT("http://dev.game.inframe.mobi/v1/micgame/round-over")
    fun sendRoundOver(@Body body: RequestBody): Call<ApiResult>

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/micgame/give-up")
    fun giveUpSing(@Body body: RequestBody): Call<ApiResult>

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/microom/req-kick-user")
    fun reqKickUser(@Body body: RequestBody): Call<ApiResult>

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/microom/exit-room")
    fun exitRoom(@Body body: RequestBody): Call<ApiResult>

    @GET("http://dev.game.inframe.mobi/v1/micgame/sync-status")
    fun syncStatus(@Query("roomID") roomID: Long): Call<ApiResult>

    /**
     * 拉取房间可邀请的段位
     */
    @GET("http://dev.game.inframe.mobi/v1/micgame/user-list")
    fun getMicSeatUserList(@Query("userID") userID: Int,
                           @Query("roomID") roomID: Int): Call<ApiResult>

    /**
     * 取消匹配
     */
    @PUT("http://dev.game.inframe.mobi/v1/microom/cancel-match")
    fun cancelMatch(@Body body: RequestBody): Call<ApiResult>

    /**
     * 进入房价
     */
    @PUT("http://dev.game.inframe.mobi/v1/microom/join-room")
    fun joinRoom(@Body body: RequestBody): Call<ApiResult>

    /**
     * 取消匹配
     */
    @PUT("http://dev.game.inframe.mobi/v1/microom/query-match")
    fun queryMatch(@Body body: RequestBody): Call<ApiResult>

    /**
     * 进入房价
     */
    @PUT("http://dev.game.inframe.mobi/v1/microom/join-room")
    fun joinRoom2(@Body body: RequestBody): Observable<ApiResult>

    @PUT("http://dev.game.inframe.mobi/v1/microom/heartbeat")
    fun heartbeat(@Body body: RequestBody): Call<ApiResult>

    @PUT("http://dev.game.inframe.mobi/v1/micgame/pk-commit-segment-result")
    fun sendPkPerSegmentResult(@Body body: RequestBody): Call<ApiResult>

    /**
     * 取消匹配EMMS_UNKNOWN = 0 : 未知 - EMMS_OPEN = 1 : match 打开 - EMMS_CLOSED = 2 : match 关闭
     */
    @PUT("http://dev.game.inframe.mobi/v1/microom/change-match-status")
    fun changeMatchStatus(@Body body: RequestBody): Call<ApiResult>
}