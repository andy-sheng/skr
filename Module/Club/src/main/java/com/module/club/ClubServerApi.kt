package com.module.club

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ClubServerApi {
    /**
     * 首页列出推荐家族(内页)
     */
    @GET("http://dev.api.inframe.mobi/v1/club/list-recommend-club-inner")
    fun getInnerRecommendClubList(@Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>

    /** 创建家族
     * {
    "desc": "string",
    "logo": "string",
    "name": "string"
    }
     */
    @POST("http://dev.api.inframe.mobi/v1/club/create")
    fun createClub(@Body body: RequestBody): Call<ApiResult>

    /**
     * 查询家族详情
     */
    @GET("http://dev.api.inframe.mobi/v1/club/detail")
    fun getClubHomePageDetail(@Query("clubID") clubID: Int): Call<ApiResult>

    /**
     * 查询家族中的成员
     */
    @GET("http://dev.api.inframe.mobi/v1/club/list-member")
    fun getClubMemberList(@Query("clubID") clubID: Int, @Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>

    /**
     * 查询家族剧场
     */
    @GET("http://dev.api.inframe.mobi/v1/club/party")
    fun getClubPartyDetail(@Query("clubID") clubID: Int): Call<ApiResult>

    /**
     * 查询家族成员剧场
     */
    @GET("http://dev.api.inframe.mobi/v1/club/list-member-party")
    fun getClubMemberPartyDetail(@Query("clubID") clubID: Int, @Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>

}