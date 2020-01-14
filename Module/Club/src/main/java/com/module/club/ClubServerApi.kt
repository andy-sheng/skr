package com.module.club

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface ClubServerApi {
    /**
     * 首页列出推荐家族
     */
    @GET("http://dev.api.inframe.mobi/v1/club/list-recommend-club")
    fun getRecommendClubList(@Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>

    /**
     * 首页列出推荐家族(内页)
     */
    @GET("http://dev.api.inframe.mobi/v1/club/list-recommend-club-inner")
    fun getInnerRecommendClubList(@Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>

    /**
     * 检查能否创建家族
     */
    @GET("http://dev.api.inframe.mobi/v1/club/check-permisson-before-create")
    fun checkCreatePermission(): Call<ApiResult>

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
     * 查询指定家族的成员信息
     */
    @GET("http://dev.api.inframe.mobi/v1/club/check-member-info")
    fun getClubMemberInfo(@Query("userID") userID: Int, @Query("clubID") clubID: Int): Call<ApiResult>


    /**
     * 查询成员申请数量，带水位
     */
    @GET("http://dev.api.inframe.mobi/v1/club/count-member-apply")
    fun getCountMemberApply(@Query("clubID") clubID: Int, @Query("lastTimeMs") lastTimeMs: Long): Call<ApiResult>

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
     * 解散家族 {null}
     */
    @PUT("http://dev.api.inframe.mobi/v1/club/dismiss-club")
    fun dismissClub(@Body body: RequestBody): Call<ApiResult>

    /**
     * 退出家族 {null}
     */
    @PUT("http://dev.api.inframe.mobi/v1/club/member-quit")
    fun existClub(@Body body: RequestBody): Call<ApiResult>

    /**
     * 改啥传啥
     * 修改家族信息 {"clubID": 0,"desc": "string","logo": "string","name": "string","notice": "string"}
     */
    @PUT("http://dev.api.inframe.mobi/v1/club/detail")
    fun editClubInfo(@Body body: RequestBody): Call<ApiResult>

    /**
     * 获取家族榜单的tag
     *
     */
    @GET("http://dev.api.inframe.mobi/v1/club/rank-tab")
    fun getClubRankTags(): Call<ApiResult>

    /**
     * 获取家族榜单的成员
     *
     */
    @GET("http://dev.api.inframe.mobi/v1/club/list-member-in-rank")
    fun getClubRankList(@Query("offset") offset: Int, @Query("cnt") cnt: Int,
                        @Query("rType") type: Int, @Query("clubID") clubID: Int): Call<ApiResult>

    /**
     * 搜索家族
     *
     */
    @GET("http://dev.api.inframe.mobi/v1/club/search-club")
    fun searchClub(@Query("keyword") keyword: String): Observable<ApiResult>


}