package com.common.core.account;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
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
    @GET("v1/passport/login-sms-code")
    Observable<ApiResult> sendSmsVerifyCode(@Query("phoneNum") String phoneNum);

    /**
     * 登陆
     *
     * @param loginType
     * @param phoneNum
     * @param verifyCode
     * @return
     */
    @GET("v1/passport/login")
    Observable<ApiResult> login(@Query("mode") int loginType,
                                @Query("sign") String phoneNum,
                                @Query("code") String verifyCode);

    /**
     * 获取IMToken
     *
     * @return
     */
    @GET("v1/messenger/token")
    Observable<ApiResult> getIMToken();

    @GET("v1/passport/logout")
    Observable<ApiResult> loginOut();

    @GET("/v1/uprofile/nickname-verification")
    Observable<ApiResult> checkNickName(@Query("nickname") String nickname);
}
