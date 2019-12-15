package com.module.club

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

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

    /**
     * 申请加入家族 {"clubID": 0,"text": "string"}
     */
    @PUT("http://dev.api.inframe.mobi/v1/club/member-join")
    fun applyJoinClub(@Body body: RequestBody): Call<ApiResult>


    /**
     * 删除家族成员 {"userID": 0}
     */
    @PUT("http://dev.api.inframe.mobi/v1/club/del-member")
    fun delClubMember(@Body body: RequestBody): Call<ApiResult>

    /**
     * 设置家族成员信息 {"userID": 0, "role": 0}
     */
    @PUT("http://dev.api.inframe.mobi/v1/club/member-info")
    fun setClubMemberInfo(@Body body: RequestBody): Call<ApiResult>

    /**
     * 审核家族成员申请 {"applyID": 0, "auditStatus": 0}
     */
    @PUT("http://dev.api.inframe.mobi/v1/club/audit-member-join")
    fun auditMemberJoin(@Body body: RequestBody): Call<ApiResult>

    /**
     * 审核家族成员申请 // 1 未审核 2审核通过 3 审核不通过
     */
    @GET("http://dev.api.inframe.mobi/v1/club/list-member-apply")
    fun getApplyMemberList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("status") status: Int): Call<ApiResult>

    /**
     * 退出家族 {null}
     */
    @PUT("http://dev.api.inframe.mobi/v1/club/member-quit")
    fun existClub(@Body body: RequestBody): Call<ApiResult>

}