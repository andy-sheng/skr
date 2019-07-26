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
    fun getFeedsRankTags(): Observable<ApiResult>


    @GET("/v1/feed/rank-song-list")
    fun getFeedRankInfoList(@Query("offset") offset: Int,
                            @Query("cnt") cnt: Int,
                            @Query("tagType") tagType: Int): Observable<ApiResult>
}