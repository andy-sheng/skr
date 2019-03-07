package com.common.core.account;

import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by chengsimin on 16/7/1.
 */
public interface UserAccountServerApi {
    /**
     * 发送短信验证码
     *
     * @return
     */
    @Headers(ApiManager.NO_NEED_LOGIN_TAG)
    @GET("v1/passport/login-sms-code")
    Observable<ApiResult> sendSmsVerifyCode(@Query("phoneNum") String phoneNum,
                                            @Query("timeMs") long timeMs,
                                            @Query("sign") String sign);

    /**
     * 登陆
     *
     * @param loginType
     * @param phoneNum
     * @param verifyCode
     * @return
     */
    @Headers(ApiManager.NO_NEED_LOGIN_TAG)
    @GET("v1/passport/login")
    Observable<ApiResult> login(@Query("mode") int loginType,
                                @Query("sign") String phoneNum,
                                @Query("code") String verifyCode,
                                @Query("platform") int platform,
                                @Query("channel") String channel,
                                @Query("deviceID") String deviceID);

    /**
     * 微信登录
     *
     * @param loginType
     * @param accessToken
     * @param openID
     * @return
     */
    @Headers(ApiManager.NO_NEED_LOGIN_TAG)
    @GET("v1/passport/login")
    Observable<ApiResult> loginWX(@Query("mode") int loginType,
                                  @Query("accessToken") String accessToken,
                                  @Query("openID") String openID,
                                  @Query("platform") int platform,
                                  @Query("channel") String channel,
                                  @Query("deviceID") String deviceID);

    /**
     * 获取IMToken
     *
     * @return
     */
    @GET("v1/messenger/token")
    Observable<ApiResult> getIMToken();

    @GET("v1/passport/logout")
    Observable<ApiResult> loginOut();

    @Headers(ApiManager.NO_NEED_LOGIN_TAG)
    @GET("/v1/uprofile/nickname-verification")
    Observable<ApiResult> checkNickName(@Query("nickname") String nickname);
}
