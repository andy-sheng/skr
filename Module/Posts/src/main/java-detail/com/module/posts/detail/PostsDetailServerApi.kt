package com.module.posts.detail

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface PostsDetailServerApi {
    /**
     * 获取一级评论
     */
    @GET("/v1/posts/first-level-comment-list")
    fun getFirstLevelCommentList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("postsID") feedID: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * 获取二级评论
     */
    @GET("/v1/posts/second-level-comment-list")
    fun getSecondLevelCommentList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("postsID") feedID: Int, @Query("commentID") commentID: Int, @Query("userID") userID: Int): Call<ApiResult>

    /**
     * {
    "like": true,
    "postsID": 0
    }
     */
    @PUT("/v1/posts/like")
    fun likePosts(@Body requestBody: RequestBody): Call<ApiResult>

    /**
     * {
    "commentID": 0,
    "like": true,
    "postsID": 0
    }
     */
    @PUT("/v1/posts/like")
    fun likeComment(@Body requestBody: RequestBody): Call<ApiResult>


    /**
     * {
    "audios": [
    {
    "URL": "string",
    "durTimeMs": 0
    }
    ],
    "content": "string",
    "firstLevelCommentID": 0,
    "pictures": [
    "string"
    ],
    "postsID": 0,
    "replyedCommentID": 0,
    "songID": 0,
    "videos": [
    {
    "URL": "string",
    "durTimeMs": 0
    }
    ]
    }
     */
    @PUT("/v1/posts/like")
    fun addComment(@Body requestBody: RequestBody): Call<ApiResult>
}