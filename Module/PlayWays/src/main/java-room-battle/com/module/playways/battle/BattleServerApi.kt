package com.module.playways.battle

import com.common.rxretrofit.ApiResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BattleServerApi {

    @GET("v1/playbook/stand-tag-list")
    fun getStandTagList(@Query("userID") userID: Long, @Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>
}