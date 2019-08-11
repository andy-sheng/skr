package com.module.feeds.detail

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface FeedsDetailServerApi {
    /**
     * 获取一级评论
     */
    @GET("/v1/feed/first-level-comment-list")
    fun getFirstLevelCommentList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("feedID") feedID: Int, @Query("userID") userID: Int): Observable<ApiResult>

    /**
     * 获取二级评论
     */
    @GET("/v1/feed/second-level-comment-list")
    fun getSecondLevelCommentList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("feedID") feedID: Int, @Query("commentID") commentID: Int, @Query("userID") userID: Int): Observable<ApiResult>

    /**
     * 点赞评论
     * {
     *  "commentID": 0,
     *  "feedID": 0,
     *  "like": true
     * }
     */
    @PUT("/v1/feed/comment-like")
    fun likeComment(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 添加评论
     *{
     *  "content": "string",
     *  "feedID": 0,
     *  "firstLevelCommentID": 0,
     *  "replyedUserID": 0
     *}
     */
    @PUT("/v1/feed/comment-add")
    fun addComment(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 点赞feeds
     *{
     *  "feedID": 0,
     *  "like": true
     *}
     */
    @PUT("/v1/feed/like")
    fun likeFeed(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 分享feeds +1
     *{
     *  "feedID": 0,
     *  "userID": 0
     *}
     */
    @PUT("/v1/feed/share")
    fun shareAdd(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 判断和指定某人的社交关系
     *
     * @param toUserID 指定人的id
     * @return
     */
    @GET("/v1/mate/has-relation")
    fun getRelation(@Query("toUserID") toUserID: Int): Observable<ApiResult>

    /**
     * 收藏feed  {
     *        "feedID": 0,
     *        "like": true
     *        }
     */
    @PUT("v1/feed/collect")
    fun collectFeed(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 用户和feed的关系
     * 全民神曲
     */
    @GET("v1/feed/user-mate")
    fun checkCollects(@Query("userID") userID: Int, @Query("feedID") feedID: Int): Observable<ApiResult>

    /**
     * 获取feed一些相关数据
     * 全民神曲
     */
    @GET("v1/feed/info")
    fun getFeedsWatchModel(@Query("userID") userID: Int, @Query("feedID") feedID: Int): Observable<ApiResult>

    /**
     * 获取被赞的列表
     */
    @GET("/v1/msgbox/like-list")
    fun getLikeWorkList(@Query("userID") userID: Int, @Query("offset") feedID: Int, @Query("cnt") cnt: Int): Call<ApiResult>

    /**
     * 获取被回复的列表
     */
    @GET("/v1/msgbox/comment-list")
    fun getRefuseCommentList(@Query("userID") userID: Int, @Query("offset") feedID: Int, @Query("cnt") cnt: Int): Call<ApiResult>
}