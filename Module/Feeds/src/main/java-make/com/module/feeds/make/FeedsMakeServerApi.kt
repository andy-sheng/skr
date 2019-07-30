package com.module.feeds.make

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface FeedsMakeServerApi {
    /**
     * 获取feeds所有标签列表
     */
    @GET("/v1/feed/challenge-song-tpl")
    suspend fun getSongTplByChallengeID(@Query("challengeID") challengeID:Long): ApiResult

    /**
     * 获取feeds所有标签列表
     */
    @GET("/v1/feed/tag-list")
    suspend fun getFeedLikeList(): ApiResult


    /**
     * 获取feeds所有标签列表
     */
    @PUT("/v1/feed/upload")
    suspend fun uploadFeeds(@Body requestBody: RequestBody): ApiResult
}