package com.wali.live.moduletest.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ReqInterface {

    @GET("sug?code=utf-8&q=%E8%A1%A3%E6%9C%8D&callback=cb")
    Call<ResponseBody> getCall();
}
