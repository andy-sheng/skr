package com.module.playways.battle.room

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface BattleRoomServerApi {

    /**
     * 演唱结束
     */
    @PUT("http://dev.game.inframe.mobi/v1/battlegame/over-sing")
    fun sendRoundOver(@Body body: RequestBody): Call<ApiResult>

    /**
     * 放弃演唱
     */
    @PUT("http://dev.game.inframe.mobi/v1/battlegame/give-up-sing")
    fun giveUpSing(@Body body: RequestBody): Call<ApiResult>

    /**
     * 演唱结束
     */
    @PUT("http://dev.game.inframe.mobi/v1/battlegame/over-sing")
    fun overSing(@Body body: RequestBody): Call<ApiResult>

    /**
     * 使用帮唱卡
     */
    @PUT("http://dev.game.inframe.mobi/v1/battlegame/req-help-sing")
    fun reqHelpSing(@Body body: RequestBody): Call<ApiResult>

    /**
     * 是否响应帮唱
     */
    @PUT("http://dev.game.inframe.mobi/v1/battlegame/rsp-help-sing")
    fun rspHelpSing(@Body body: RequestBody): Call<ApiResult>

    /**
     * 使用换歌卡
     */
    @PUT("http://dev.game.inframe.mobi/v1/battlegame/req-switch-sing")
    fun reqSwitchSing(@Body body: RequestBody): Call<ApiResult>

    /**
     * 开始演唱
     */
    @PUT("http://dev.game.inframe.mobi/v1/battlegame/begin-sing")
    fun grabSing(@Body body: RequestBody): Call<ApiResult>

    /**
     * 退出房间
     */
    @PUT("http://dev.game.inframe.mobi/v1/battlegame/user—exit")
    fun userExit(@Body body: RequestBody): Call<ApiResult>

    /**
     * 同步
     */
    @GET("http://dev.game.inframe.mobi/v1/battlegame/sync-status")
    fun syncStatus(@Query("roomID") roomID: Long): Call<ApiResult>

    @PUT("http://dev.game.inframe.mobi/v1/battlegame/pk-commit-segment-result")
    fun sendPkPerSegmentResult(@Body body: RequestBody): Call<ApiResult>
}
