package com.common.upload;



import com.alibaba.fastjson.JSONObject;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface UploadAppServerApi {

    /**
     * 获取上传sts token
     *
     * @return
     */
    @GET("http://dev.res.inframe.mobi/v1/getSTSToken/picture")
    Observable<JSONObject> getSTSToken();


}
