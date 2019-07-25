package com.component.feeds

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface FeedsWatchServerApi {
    /**
     * 获取推荐列表
     */
    @GET("/v1/feed/recommend-list")
    fun getFeedRecommendList(@Query("offset") offset: Int, @Query("cnt") cnt: Int): Observable<ApiResult>

    /**
     * 获取关注列表
     */
    @GET("/v1/feed/follow-list")
    fun getFeedFollowList(@Query("offset") offset: Int, @Query("cnt") cnt: Int): Observable<ApiResult>

    /**
     * 获取喜欢列表
     */
    @GET("/v1/feed/like-list")
    fun getFeedLikeList(@Query("offset") offset: Int, @Query("cnt") cnt: Int): Observable<ApiResult>


    /**
     * 获取个人主页上神曲列表
     *
     * feedSongType  1 所有神曲（包括未审核和审核成功状态）
     *               2 审核通过的神曲
     */
    @GET("/v1/feed/query-feedsongs")
    fun queryFeedsList(@Query("offset") offset: Int,
                       @Query("cnt") cnt: Int,
                       @Query("userID") userID: Int,
                       @Query("feedSongType") feedSongType: Int): Observable<ApiResult>
}