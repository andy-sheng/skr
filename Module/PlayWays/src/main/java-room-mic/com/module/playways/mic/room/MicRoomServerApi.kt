package com.module.playways.mic.room

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface MicRoomServerApi {

    @GET("http://dev.game.inframe.mobi/v2/raceroom/check-rank")
    abstract fun checkRank(@Query("roomType") roomType: Int): Call<ApiResult>

}