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
    @PUT("http://dev.game.inframe.mobi/v1/microom/kickout")
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

    /**
     * 从外面邀请之后同意，调用这个接口查询房间信息
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/invite-user-enter")
    fun relayInviteUserEnter(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 从房间内邀请，邀请之后同意，调用这个接口查询房间信息
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/room-invite-user-enter")
    fun relayRoomInviteUserEnter(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 拒绝加入合唱房
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/refuse-enter")
    fun relayRefuseEnter(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 从房间内邀请，邀请之后同意，调用这个接口查询房间信息
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/redpacket-invite-user-enter")
    fun relayRoomRedPacketInviteUserEnter(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 拒绝加入红包合唱房
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/redpacket-refuse-enter")
    fun relayRefuseRedPacketEnter(@Body body: RequestBody): Observable<ApiResult>
}