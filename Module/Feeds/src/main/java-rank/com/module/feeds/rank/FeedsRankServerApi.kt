package com.module.feeds.rank

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface FeedsRankServerApi {

    /**
     * 全民神曲：神曲榜单标签列表
     */
    @GET("v1/feed/rank-song-tag-list")
    suspend fun getFeedsRankTags(): ApiResult


    @GET("/v1/feed/rank-song-list")
    suspend fun getFeedRankInfoList(@Query("offset") offset: Int,
                                    @Query("cnt") cnt: Int,
                                    @Query("tagType") tagType: Int): ApiResult


    @GET("v1/feed/rank-category-list")
    suspend fun getFeedRankDetailList(@Query("offset") offset: Int,
                                      @Query("cnt") cnt: Int,
                                      @Query("userID") userID: Int,
                                      @Query("challengeID") challengeID: Long): ApiResult


    @GET("v1/feed/search-challenge")
    fun searchChallenge(@Query("searchContent") searchContent: String): Observable<ApiResult>
}