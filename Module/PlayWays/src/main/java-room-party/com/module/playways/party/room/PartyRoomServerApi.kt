package com.module.playways.party.room

import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
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

    @GET("http://dev.game.inframe.mobi/v1/partygame/sync-status")
    fun syncStatus(@Query("roomID") roomID: Long): Call<ApiResult>

    /**
     * {
    "adminUserID": 0,
    "roomID": 0,
    "setType": "SAT_UNKNOWN"  SAT_UNKNOWN, SAT_ADD, SAT_DEL
    }
     */
    @PUT("http://dev.game.inframe.mobi/v1/partygame/set-admin")
    fun setAdmin(@Body body: RequestBody): Call<ApiResult>

    /**
     * {
    "notice": "string",
    "roomID": 0
    }
     */
    @PUT("http://dev.game.inframe.mobi/v1/partyroom/set-notice")
    fun setNotice(@Body body: RequestBody): Call<ApiResult>

    /**
     * 获取表情列表
     */
    @GET("http://dev.game.inframe.mobi/v1/partygame/list-emoji")
    fun getEmojiList(): Call<ApiResult>

    /**
     * 发送表情 {"id": 0, "roomID": 0 }
     */
    @PUT("http://dev.game.inframe.mobi/v1/partygame/send-emoji")
    fun sendEmoji(@Body body: RequestBody): Call<ApiResult>

}