package com.module.playways.battle

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface BattleServerApi {

    @GET("v1/playbook/stand-tag-list")
    fun getStandTagList(@Query("userID") userID: Long, @Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>

    @GET("v1/playbook/stand-song-list")
    fun getStandSongList(@Query("userID") userID: Long, @Query("tagID") tagID: Int): Call<ApiResult>

    @GET("v1/playbook/stand-mine")
    fun getStandRankMine(@Query("userID") userID: Long, @Query("tagID") tagID: Int): Call<ApiResult>

    @GET("v1/playbook/stand-rank-tabs")
    fun getStandRankTag(): Call<ApiResult>

    @GET("v1/playbook/stand-rank-list")
    fun getStandRankList(@Query("userID") userID: Long,
                         @Query("tagID") tagID: Int,
                         @Query("offset") offset: Int,
                         @Query("cnt") cnt: Int,
                         @Query("tabType") tabType: Int): Call<ApiResult>

    // 开启歌单
    @PUT("v1/playbook/stand-tag-enable")
    fun enableStandTag(@Body body: RequestBody): Call<ApiResult>
}