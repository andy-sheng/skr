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

    /**
     * 收藏歌单  {
     *        "albumID": 0,
     *        "isCollected": true,
     *        "userID": 0
     *          }
     */
    @PUT("/v1/feed/album-collect")
    fun albumCollect(@Body body: RequestBody): Call<ApiResult>

    /**
     * 分享feeds +1
     *{
     *  "feedID": 0,
     *  "userID": 0
     *}
     */
    @PUT("/v1/feed/share")
    fun shareAdd(@Body body: RequestBody): Call<ApiResult>

    /**
     * 全民神曲，分页获取全量的feed收藏信息
     */
    @GET("v1/feed/collect-list-by-page")
    fun getCollectListByPage(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Long): Call<ApiResult>

    /**
     * 全民神曲：按照indexID拉去feed收藏信息
     */
    @GET("v1/feed/collect-list-by-index-id")
    fun getCollectListByIndex(@Query("lastIndexID") lastIndexID: Long, @Query("userID") userID: Long): Call<ApiResult>

    @GET("v1/feed/rank-tag-category")
    fun getRecomendTagList(@Query("offset") offset: Int,
                           @Query("cnt") cnt: Int,
                           @Query("userID") userID: Long): Call<ApiResult>

    @GET("v1/feed/album-collect-list")
    fun getAlbumCollectList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Long): Call<ApiResult>

    @GET("/v1/feed/check-album-collect")
    fun checkAlbumCollect(@Query("albumID") albumID: Int, @Query("userID") userID: Long): Call<ApiResult>
    /**
     * 全民神曲：根据榜单标签分类获取歌单数据
     */
    @GET("v1/feed/rank-song-list-by-tag")
    fun getRecomendTagDetailList(@Query("offset") offset: Int,
                                 @Query("cnt") cnt: Int,
                                 @Query("rankID") rankID: Int,
                                 @Query("queryDate") queryDate: String,
                                 @Query("userID") userID: Long): Call<ApiResult>

}