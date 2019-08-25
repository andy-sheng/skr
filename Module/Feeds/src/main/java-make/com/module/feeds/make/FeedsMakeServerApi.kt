package com.module.feeds.make

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface FeedsMakeServerApi {
    /**
     * 获取feeds所有标签列表
     */
    @GET("/v1/feed/challenge-song-tpl")
    fun getSongTplByChallengeID(@Query("challengeID") challengeID: Long): Call<ApiResult>

    /**
     * 获取feeds所有标签列表
     */
    @GET("/v1/feed/tag-list")
    fun getFeedLikeList(@Query("tagType") tagType: Int): Call<ApiResult>


    /**
     * 获取feeds所有标签列表
     */
    @PUT("/v1/feed/upload")
    fun uploadHitFeeds(@Body requestBody: RequestBody): Call<ApiResult>

    /**
     * 上传翻唱
     */
    @PUT("/v1/feed/common-fanchang-upload")
    fun uploadQuickFeeds(@Body requestBody: RequestBody): Call<ApiResult>

    /**
     * 上传改编
     */
    @PUT("/v1/feed/common-gaibian-upload")
    fun uploadChangeFeeds(@Body requestBody: RequestBody): Call<ApiResult>

    /**
     * 打榜上传翻唱
     */
    @PUT("/v1/feed/fanchang-upload")
    fun uploadHitQuickFeeds(@Body requestBody: RequestBody): Call<ApiResult>

    /**
     * 打榜上传改编
     */
    @PUT("/v1/feed/gaibian-upload")
    fun uploadHitChangeFeeds(@Body requestBody: RequestBody): Call<ApiResult>
}