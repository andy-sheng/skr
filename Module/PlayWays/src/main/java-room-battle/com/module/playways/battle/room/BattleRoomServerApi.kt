package com.module.playways.battle.room

import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

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
