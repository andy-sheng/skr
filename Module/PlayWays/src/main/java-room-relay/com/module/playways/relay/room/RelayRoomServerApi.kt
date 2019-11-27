package com.module.playways.relay.room

import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RelayRoomServerApi {

    @PUT("http://dev.game.inframe.mobi/v1/relaygame/round-over")
    fun sendRoundOver(@Body body: RequestBody): Call<ApiResult>

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/give-up")
    fun giveUpSing(@Body body: RequestBody): Call<ApiResult>

    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/exit-room")
    fun exitRoom(@Body body: RequestBody): Call<ApiResult>

    @PUT("http://dev.game.inframe.mobi/v1/relaygame/heartbeat")
    fun heartbeat(@Body body: RequestBody): Call<ApiResult>

    @GET("http://dev.game.inframe.mobi/v1/relaygame/sync-status")
    fun syncStatus(@Query("roomID") roomID: Long): Call<ApiResult>

    @PUT("http://dev.game.inframe.mobi/v1/relaygame/unlock-user-info")
    fun sendUnlock(body: RequestBody): Call<ApiResult>



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


    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/microom/kickout")
    fun reqKickUser(@Body body: RequestBody): Call<ApiResult>




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

    @PUT("http://dev.game.inframe.mobi/v1/micgame/pk-commit-segment-result")
    fun sendPkPerSegmentResult(@Body body: RequestBody): Call<ApiResult>

    /**
     * 取消匹配EMMS_UNKNOWN = 0 : 未知 - EMMS_OPEN = 1 : match 打开 - EMMS_CLOSED = 2 : match 关闭
     */
    @PUT("http://dev.game.inframe.mobi/v1/microom/change-match-status")
    fun changeMatchStatus(@Body body: RequestBody): Call<ApiResult>

    /**
     *  同意合唱/PK的结果 {"roomID": 0,"uniqTag": "string"}
     */
    @PUT("http://dev.game.inframe.mobi/v1/micgame/agree-sing")
    fun agreeSing(@Body body: RequestBody): Call<ApiResult>

    /**
     *  查询同意合唱/PK的结果 {"roomID": 0,"uniqTag": "string"}
     */
    @GET("http://dev.game.inframe.mobi/v1/micgame/agree-sing-result")
    fun getAgreeSingResult(@Query("roomID") roomID: Int, @Query("uniqTag") uniqTag: String): Call<ApiResult>

    /**
     *  拉取已点歌曲数量
     */
    @GET("http://dev.game.inframe.mobi/v1/micgame/get-add-music-cnt")
    fun getAddMusicCnt(@Query("roomID") roomID: Int, @Query("userID") userID: Int): Call<ApiResult>

    @PUT("http://dev.game.inframe.mobi/v1/game/user-statistic")
    fun userStatistic(@Body body: RequestBody): Call<ApiResult>


}