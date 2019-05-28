package com.common.upload;


import com.alibaba.fastjson.JSONObject;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UploadAppServerApi {

    /**
     * 获取上传sts token
     *
     * @return
     */
    @GET("http://dev.res.inframe.mobi/v1/getSTSToken/{dir}")
    Observable<JSONObject> getSTSToken(@Path("dir") String dir);


}
