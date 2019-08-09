package com.module.feeds.songmanage

import com.common.rxretrofit.ApiResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FeedSongManageServerApi {
    /**
     * 获取普通歌曲标签
     */
    @GET("/v1/feed/common-song-tag-list")
    fun getFeedSongTagList(): Call<ApiResult>

    /**
     * 获取普通歌曲列表
     */
    @GET("/v1/feed/common-song-list")
    fun getFeedSongList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("tagType") tagType: Int): Call<ApiResult>
}