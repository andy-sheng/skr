package com.common.core.myinfo;


import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface MyUserInfoServerApi {

    @GET("v1/uprofile/information")
    Call<ApiResult> getUserInfo(@Query("userID") int userID);

    @PUT("v1/uprofile/information")
    Observable<ApiResult> updateInfo(@Body RequestBody body);

    /**
     *  检查昵称
     * @param nickname 昵称
     * @return
     */
    @GET("v1/uprofile/nickname-verification")
    Observable<ApiResult> checkNickName(@Query("nickname") String nickname);


    @GET("v1/uprofile/verify-sensitive-name")
    Observable<ApiResult> verifyName(@Query("nickname") String nickname);
}
