package com.module.home;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
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


    /**
     * 绑定微信
     * {
     *   "openID": "string",
     *   "unionID": "string"
     * }
     * @param body
     * @return
     */
    @PUT("/v1/wallet/bind-wx")
    Observable<ApiResult> authBindWX(@Body RequestBody body);

    /**
     * 检测短信验证码是否正确+手机号是否绑定
     * {
     *   "code": "string",
     *   "phone": "string"
     * }
     * @param body
     * @return
     */
    @PUT("/v1/wallet/check-sms")
    Observable<ApiResult> checkSMSCode(@Body RequestBody body);

    /**
     * 发送短信验证
     * {
     *   "phone": "string",
     *   "sign": "string",
     *   "timeMs": 0
     * }
     * @param body
     * @return
     */
    @PUT("/v1/wallet/send-sms")
    Observable<ApiResult> sendSMSCode(@Body RequestBody body);

    /**
     * 提现认证
     * {
     *   "bizNo": "string",
     *   "iDCard": "string",
     *   "name": "string"
     * }
     * @param body
     * @return
     */
    @PUT("/v1/wallet/withdraw-auth")
    Observable<ApiResult> withdrawAuth(@Body RequestBody body);

    /**
     * 提现
     * {
     *   "action": "EW_UNKNOWN",
     *   "amount": "string"
     * }
     * @param body
     * @return
     */
    @PUT("/v1/wallet/withdraw-decr")
    Observable<ApiResult> decr(@Body RequestBody body);

    /**
     * 提现页面信息
     * @return
     */
    @GET("/v1/wallet/withdraw-info")
    Observable<ApiResult> getWithdrawInfo();

    /**
     * 提现流水
     * @return
     */
    @GET("/v1/wallet/withdraw-records")
    Observable<ApiResult> getListWithdraw(@Query("offset") int offset,
                                          @Query("limit") int limit);
}
