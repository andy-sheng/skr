package com.module.feeds.watch

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface FeedsWatchServerApi {
    /**
     * 获取推荐列表
     */
    @GET("/v1/feed/recommend-list")
    fun getFeedRecommendList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * 获取关注列表
     */
    @GET("/v1/feed/follow-list")
    fun getFeedFollowList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * 获取喜欢列表
     */
    @GET("/v1/feed/like-list")
    fun getFeedLikeList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Observable<ApiResult>


    /**
     * 获取收藏列表
     */
    @GET("/v1/feed/collect-list")
    fun getFeedCollectList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>


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
                       @Query("feedUserID") feedUserID: Int,
                       @Query("feedSongType") feedSongType: Int): Call<ApiResult>


    /**
     * 点赞/取消点赞feed
     * feedID
     * like ture/false
     */
    @PUT("/v1/feed/like")
    fun feedLike(@Body body: RequestBody): Call<ApiResult>


    @PUT("v1/feed/delete-song")
    fun deleteFeed(@Body body: RequestBody): Call<ApiResult>


    /**
     * 判断和指定某人的社交关系
     *
     * @param toUserID 指定人的id
     * @return
     */
    @GET("/v1/mate/has-relation")
    fun getRelation(@Query("toUserID") toUserID: Int): Call<ApiResult>

    /**
     * 用户和feed的关系
     * 全民神曲
     */
    @GET("v1/feed/user-mate")
    fun checkCollects(@Query("userID") userID: Int, @Query("feedID") feedID: Int): Call<ApiResult>

    /**
     * 收藏feed  {
     *        "feedID": 0,
     *        "like": true
     *        }
     */
    @PUT("v1/feed/collect")
    fun collectFeed(@Body body: RequestBody): Call<ApiResult>

}