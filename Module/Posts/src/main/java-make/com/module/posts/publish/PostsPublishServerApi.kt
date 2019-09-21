package com.module.posts.publish

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface PostsPublishServerApi {

    /**
     * 获取红包信息
     */
    @GET("/v1/posts/redpacket-info")
    fun getRedpacketInfo(): Call<ApiResult>

    @PUT("/v1/posts/upload")
    fun uploadPosts(@Body requestBody: RequestBody): Call<ApiResult>
}