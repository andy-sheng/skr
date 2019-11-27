package com.module.playways.relay.match

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface RelayMatchServerApi {

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
}