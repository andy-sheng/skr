package com.module.club

import com.common.rxretrofit.ApiResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ClubServerApi {
    /**
     * 首页列出推荐家族(内页)
     */
    @GET("http://dev.api.inframe.mobi/v1/club/list-recommend-club-inner")
    fun getInnerRecommendClubList(@Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>



}