package com.module.playways.relay.match

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface RelayMatchServerApi {

    /**
     * 查询红包合唱状态 0UNKOWN 1打开 2关闭
     */
    @GET("http://dev.game.inframe.mobi/v1/relaygame/redpacket-get-bell-status")
    fun getRedPacketStatus(): Call<ApiResult>

    /**
     * 更新合唱   true为打开  false为关闭
     */
    @GET("http://dev.game.inframe.mobi/v1/relaygame/redpacket-update-bell-status")
    fun updateRedPacketStatus(@Query("enable") enable: Boolean): Call<ApiResult>

    /**
     * 发送红包合唱邀请
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/redpacket-send-invite-user")
    fun sendRedPacketInvite(@Body body: RequestBody): Call<ApiResult>

    /**
     * 检查用户进入权限
     */
    @GET("http://dev.game.inframe.mobi/v1/relaygame/enter-permission")
    fun checkEnterPermission(): Call<ApiResult>

    /**
     * 列出可以接唱的曲目
     */
    @GET("http://dev.game.inframe.mobi/v1/relaygame/list-playbook-items")
    fun getPlayBookList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * 列出匹配中的列表
     */
    @GET("http://dev.game.inframe.mobi/v1/relaygame/list-match-items")
    fun getMatchRoomList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>


    /**
     * 请求匹配
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/query-match")
    fun queryMatch(@Body body: RequestBody): Call<ApiResult>


    /**
     * 请求匹配
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/cancel-match")
    fun cancelMatch(@Body body: RequestBody): Call<ApiResult>


    /**
     * 选中匹配列表中
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/choice-match-item")
    fun choiceRoom(@Body body: RequestBody): Call<ApiResult>

    /**
     * 选中匹配列表中
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/enter-failed")
    fun enterRoomFailed(@Body body: RequestBody): Call<ApiResult>

    /**
     * 获取用户每日剩余开房次数
     */
    @GET("http://dev.game.inframe.mobi/v1/relaygame/room-data")
    fun getTotalResTimes(): Call<ApiResult>

    @PUT("http://dev.game.inframe.mobi/v1/relaygame/room-create")
    fun createRelayRoom(@Body body: RequestBody): Call<ApiResult>

    /**
     * 获取已点的列表
     *
     * @param offset
     * @param cnt
     * @return 包含  歌曲信息 items（List）
     * 偏移量 offset
     */
    @GET("http://dev.game.inframe.mobi/v1/relaygame/list-history-playbook-items")
    fun getRelayClickedMusicItmes(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>
}