package com.module.playways.room.gift;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface GiftServerApi {

    /**
     * 根据游戏model列出剧本
     *
     * @return
     */
    @GET("/v2/gift/list")
    Observable<ApiResult> getGiftList(@Query("offset") int offset,
                                      @Query("limit") int limit
    );

    @PUT("/v3/gift/present-gift")
    Observable<ApiResult> buyGift(@Body RequestBody body);

    /**
     * 获取钻石余额
     *
     * @return
     */
    @GET("/v1/wallet/zs-balance")
    Observable<ApiResult> getZSBalance();

    /**
     * 获取金币数量
     *
     * @return
     */
    @GET("http://dev.stand.inframe.mobi/v1/stand/coin-cnt")
    Observable<ApiResult> getCoinNum();
}

