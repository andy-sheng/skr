package com.module.mall

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface MallServerApi {

    /**
     * 获取商品标签
     *
     * @return
     */
    @GET("http://sandbox.api.inframe.mobi/v1/mall/display-type-tag")
    fun getMallDisplayTags(): Call<ApiResult>

    /**
     * 展示排行榜数据
     */
    @GET("http://sandbox.api.inframe.mobi/v1/mall/goods-list")
    fun getProductList(@Query("displayType") displayType: Int, @Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>

    /**
     * 获取我的排行榜数据
     */
    @GET("http://sandbox.api.inframe.mobi/v1/mall/my-packet-list")
    fun getPacketList(@Query("displayType") displayType: Int, @Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>

    /**
     * {
    "buyType": "BT_UNKNOWN",
    "count": 0,
    "goodsID": 0,
    "priceType": "PT_UNKNOWN"
    }
     */
    @PUT("http://sandbox.api.inframe.mobi/v1/mall/buy-goods")
    fun buyMall(@Body body: RequestBody): Call<ApiResult>

    /**
    {
    "packetItemID": "string"
    }
     */
    @PUT("http://sandbox.api.inframe.mobi/v1/mall/use-goods")
    fun useGoods(@Body body: RequestBody): Call<ApiResult>

    /**
    {
    "packetItemID": "string"
    }
     */
    @PUT("http://sandbox.api.inframe.mobi/v1/mall/use-goods")
    fun cancelUseGoods(@Body body: RequestBody): Call<ApiResult>
}
