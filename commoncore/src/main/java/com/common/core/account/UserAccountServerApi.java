package com.common.core.account;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
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
    Observable<ResponseBody> sendSmsVerifyCode(@Query("phoneNum") String phoneNum);

    @GET("v1/passport/login")
    Observable<ResponseBody> login(@Query("mode") int loginType,
                                   @Query("sign") String phoneNum,
                                   @Query("code") String verifyCode);
}
