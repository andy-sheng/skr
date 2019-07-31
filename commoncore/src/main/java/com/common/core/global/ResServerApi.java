package com.common.core.global;

import com.alibaba.fastjson.JSONObject;
import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ResServerApi {

    @GET("http://dev.res.inframe.mobi/v1/oss/download?type=1")
    Call<JSONObject> getLyricByUrl(@Query("URL") String url);
}
