package com.module.posts.watch

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface PostsWatchServerApi {

    /**
     * 获取关注列表
     */
    @GET("/v1/posts/follow-list")
    fun getPostsFollowList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * 获取推荐列表
     */
    @GET("/v1/posts/recommend-list")
    fun getPostsRecommendList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * 获取最新列表
     */
    @GET("/v1/posts/newest-list")
    fun getPostsLastList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * 获取帖子话题详情
     */
    @GET("/v1/posts/topic-detail")
    fun getTopicDetail(@Query("userID") userID: Long, @Query("topicID") topicID: Long): Call<ApiResult>

    /**
     * 话题场景tab列表
     */
    @GET("/v1/posts/topic-tabs")
    fun getTopicTabs(@Query("topicID") topicID: Long): Call<ApiResult>

    /**
     * 话题场景 帖子列表
     */
    @GET("/v1/posts/topic-posts-list")
    fun getTopicPostsList(@Query("offset") offset: Int,
                          @Query("cnt") cnt: Int,
                          @Query("userID") userID: Long,
                          @Query("topicID") topicID: Long,
                          @Query("tab") tab: Int): Call<ApiResult>

}