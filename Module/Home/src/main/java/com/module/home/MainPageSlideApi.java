package com.module.home;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 *
 */
public interface MainPageSlideApi {

    /**
     * 拿到某个人基本的信息
     * @return
     */
    @GET("v1/kconf/slide-show")
    Observable<ApiResult> getSlideList();
}
