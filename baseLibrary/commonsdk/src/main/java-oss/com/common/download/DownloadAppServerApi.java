package com.common.download;



import com.alibaba.fastjson.JSONObject;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface DownloadAppServerApi {

    /**
     * 获取上传sts token
     *
     * @return
     */
    @GET("http://dev.i.res.inframe.mobi/v1/getSTSToken/picture")
    Observable<JSONObject> getSTSToken();


}
