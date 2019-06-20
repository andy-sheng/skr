package com.module.home.ranked

import com.common.rxretrofit.ApiResult

import io.reactivex.Observable
import retrofit2.http.GET

interface RankedServerApi {

    /**
     * 首页排行榜卡片数据
     *
     * @return
     */
    @get:GET("http://sandbox.api.inframe.mobi/v1/rank/home-rank-cards")
    val homeRankCards: Observable<ApiResult>

    val listRankData: Observable<ApiResult>

    val myRank: Observable<ApiResult>
}
