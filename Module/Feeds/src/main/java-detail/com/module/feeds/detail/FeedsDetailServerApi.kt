package com.module.feeds.detail

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface FeedsDetailServerApi {
    /**
     * 获取一级评论
     */
    @GET("/v1/feed/first-level-comment-list")
    fun getFirstLevelCommentList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("feedID") feedID: Int): Observable<ApiResult>

    /**
     * 获取二级评论
     */
    @GET("/v1/feed/second-level-comment-list")
    fun getSecondLevelCommentList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("commentID") commentID: Int): Observable<ApiResult>

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
}