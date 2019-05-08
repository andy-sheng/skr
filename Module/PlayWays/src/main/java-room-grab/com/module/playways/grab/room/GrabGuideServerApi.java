package com.module.playways.grab.room;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface GrabGuideServerApi {


    /**
     * 获取新手引导资源
     * @return
     */
    @GET("http://dev.kconf.inframe.mobi/v1/kconf/beginner-guide-resources")
    Observable<ApiResult> getGuideRes(@Query("tagID") int tagID);

}
