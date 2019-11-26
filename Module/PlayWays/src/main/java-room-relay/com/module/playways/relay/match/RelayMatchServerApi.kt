package com.module.playways.relay.match

import com.common.rxretrofit.ApiResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RelayMatchServerApi {

    /**
     * 列出可以接唱的曲目
     */
    @GET("/v1/relaygame/list-playbook-items")
    fun getPlayBookList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * 列出匹配中的列表
     */
    @GET("/v1/relaygame/list-match-items")
    fun getMatchRoomList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>
}