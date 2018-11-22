package com.wali.live.moduletest.retrofit;

import io.reactivex.Observable;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.http.GET;

/**
 * 商城服务
 */
public interface MallService {

    /**
     * 得到商品
     * @return
     */
    @GET("sug?code=utf-8&q=%E8%A1%A3%E6%9C%8D&callback=cb")
    Observable<ResponseBody> getGoods();
}
