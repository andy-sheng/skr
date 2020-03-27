package com.component.club

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface ClubMemberServerApi {
    /**
     * 查询家族中的成员
     */
    @GET("http://dev.api.inframe.mobi/v1/club/list-member")
    fun getClubMemberList(@Query("clubID") clubID: Int, @Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>

    /**
     * 检查是否申请加入该家族
     */
    @GET("http://dev.api.inframe.mobi/v1/club/has-applied-join")
    fun hasAppliedJoin(@Query("clubID") clubID: Int): Call<ApiResult>

    /**
     * 取消申请加入
     */
    @PUT("http://dev.api.inframe.mobi/v1/club/cancel-applied-join")
    fun cancelApplyJoin(@Body body: RequestBody): Call<ApiResult>

    /**
     * 申请加入家族 {"clubID": 0,"text": "string"}
     */
    @PUT("http://dev.api.inframe.mobi/v1/club/member-join")
    fun applyJoinClub(@Body body: RequestBody): Call<ApiResult>
}