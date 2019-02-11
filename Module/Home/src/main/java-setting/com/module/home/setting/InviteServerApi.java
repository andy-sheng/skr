package com.module.home.setting;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface InviteServerApi {

    /**
     * 获取拉新验证的短信码
     * @param phoneNum   [必传]手机号码
     * @param timeMs     [必传]当前毫秒时间戳
     * @param sign       [必传]签名 md5(skrer|invite|phoneNum|timeMs)
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/event/invite-sms-code")
    Observable<ApiResult> getInviteSmsCode(@Query("phoneNum")String phoneNum,
                                           @Query("timeMs")String timeMs,
                                           @Query("sign")String sign);


    /**
     * 提交拉新邀请
     * @param body   [必传]手机号码
     *               [必传]手机验证码
     *               [必传]邀请码
     * @return
     */
    @PUT("http://dev.api.inframe.mobi/v1/event/invite-record")
    Observable<ApiResult> submitInviteCode(@Body RequestBody body);
}
