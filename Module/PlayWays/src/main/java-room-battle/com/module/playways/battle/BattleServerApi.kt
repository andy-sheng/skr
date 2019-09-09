package com.module.playways.battle

import com.common.rxretrofit.ApiResult
import retrofit2.Call
import retrofit2.http.GET
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
}