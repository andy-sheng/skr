package com.module.posts.publish

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface PostsPublishServerApi {

    /**
     * 获取红包信息
     */
    @GET("/v1/posts/redpacket-info")
    fun getRedpacketInfo(): Call<ApiResult>

    @PUT("/v1/posts/upload")
    fun uploadPosts(@Body requestBody: RequestBody): Call<ApiResult>

    @GET("/v1/posts/topic-category-list")
    fun getTopicCategoryList(@Query("offset") offset: Int,
                             @Query("cnt") cnt: Int,
                             @Query("userID") userID: Int): Call<ApiResult>

    @GET("/v1/posts/topic-list")
    fun getTopicList(@Query("offset") offset: Int,
                             @Query("cnt") cnt: Int,
                             @Query("userID") userID: Int,
                             @Query("categoryID") categoryID: Int): Call<ApiResult>
}