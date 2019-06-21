package com.module.home.ranked

import com.common.rxretrofit.ApiResult

import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface RankedServerApi {

    /**
     * 首页排行榜卡片数据
     *
     * @return
     */
    @GET("http://sandbox.api.inframe.mobi/v1/rank/home-rank-cards")
    fun homeRankCards(): Observable<ApiResult>

    /**
     * 展示排行榜数据
     */
    @GET("http://sandbox.api.inframe.mobi/v1/rank/list-rank-data")
    fun listRankData(@Query("rankID") rankID: Int, @Query("offset") offset: Int, @Query("limit") limit: Int): Observable<ApiResult>

    /**
     * 获取我的排行榜数据
     */
    @GET("http://sandbox.api.inframe.mobi/v1/rank/get-my-rank")
    fun getMyRank(@Query("rankID") rankID: Int): Observable<ApiResult>
}
