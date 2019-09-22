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


    @GET("/v1/posts/redpacket-detail")
    fun getRedPkgDetail(@Query("userID") userID: Long, @Query("redpacketID") redpacketID: Long): Call<ApiResult>

    /**
     * 个人主页，帖子列表
     * postsListType EPHLT_ALL = 1 : 所有的帖子（未审核状态+审核成功状态） EPHLT_AUDIT_SUCCESS = 2 : 审核通过的帖子
     */
    @GET("/v1/posts/homepage-posts-list")
    fun getHomePagePostsList(@Query("offset") offset: Int,
                             @Query("cnt") cnt: Int,
                             @Query("userID") userID: Long,
                             @Query("postsUserID") postsUserID: Long,
                             @Query("postsListType") postsListType: Int): Call<ApiResult>

    /**
     * 点赞或取消赞 帖子   "like": true,"postsID": 0
     */
    @PUT("/v1/posts/like")
    fun postsLikeOrUnLike(@Body body: RequestBody): Call<ApiResult>

    /**
     * 点赞或取消赞 帖子   "like": true,"postsID": 0 ,"commentID": 0,
     */
    @PUT("/v1/posts/like")
    fun postsCommentLikeOrUnLike(@Body body: RequestBody): Call<ApiResult>

    @PUT("/v1/posts/delete")
    fun deletePosts(@Body body: RequestBody): Call<ApiResult>
}