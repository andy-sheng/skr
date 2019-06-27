package com.component.busilib.recommend;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RAServerApi {
    @GET("http://dev.game.inframe.mobi/v1/kconf/abtest-info")
    Observable<ApiResult> getABtestInfo(@Query("testList") String testList);
}
