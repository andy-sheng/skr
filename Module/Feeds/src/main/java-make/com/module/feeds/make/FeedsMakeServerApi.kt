package com.module.feeds.make

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface FeedsMakeServerApi{
    /**
     * 获取feeds所有标签列表
     */
    @GET("/v1/feed/tag-list")
    suspend fun getFeedLikeList(): ApiResult

}