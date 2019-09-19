package com.module.posts.detail

import com.common.rxretrofit.ApiResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PostsDetailServerApi {
    /**
     * 获取一级评论
     */
    @GET("/v1/feed/first-level-comment-list")
    fun getFirstLevelCommentList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("postsID") feedID: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * 获取二级评论
     */
    @GET("/v1/feed/second-level-comment-list")
    fun getSecondLevelCommentList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("postsID") feedID: Int, @Query("commentID") commentID: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * 判断和指定某人的社交关系
     *
     * @param toUserID 指定人的id
     * @return
     */
    @GET("/v1/mate/has-relation")
    fun getRelation(@Query("toUserID") toUserID: Int): Call<ApiResult>
}