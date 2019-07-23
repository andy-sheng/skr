package com.module.feeds.detail

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface FeedsDetailServerApi {
    /**
     * 获取推荐列表
     */
    @GET("/v1/feed/first-level-comment-list")
    fun getFirstLevelCommentList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("feedID") feedID: Int): Observable<ApiResult>

    /**
     * 获取关注列表
     */
    @GET("/v1/feed/second-level-comment-list")
    fun getFeedFollowList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("commentID") commentID: Int): Observable<ApiResult>
}