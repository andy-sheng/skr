package com.module.home;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WalletServerApi {

    /**
     * 获取余额
     *
     * @return
     */
    @GET("/v1/wallet/balance")
    Observable<ApiResult> getBalance();

    /**
     * 获取钱包流水
     *
     * @param offset
     * @param cnt
     * @return 包含  歌曲信息 items（List）
     * 偏移量 offset
     */
    /**
     * 获取钱包流水
     *
     * @param offset 偏移量
     * @param limit  拉取数量
     * @param action 模式     0未知   1收入   2提现
     * @return
     */
    @GET("/v1/wallet/money-records")
    Observable<ApiResult> getWalletRecord(@Query("offset") int offset,
                                             @Query("limit") int limit,
                                             @Query("action") int action);


}
