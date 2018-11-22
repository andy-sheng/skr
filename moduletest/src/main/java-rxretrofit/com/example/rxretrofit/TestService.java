package com.example.rxretrofit;

import com.alibaba.fastjson.JSONObject;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 商城服务
 */
public interface TestService {

    /**
     * 得到商品
     * @return
     */
    @GET("https://suggest.taobao.com/sug?code=utf-8")
    Observable<JSONObject> getGoods(@Query("q") String searchKey);

    /**
     * 得到商品
     * @return
     */
    @GET("https://www.sojson.com/api/qqmusic/8446666")
    Observable<JSONObject> getCartoons();
}
