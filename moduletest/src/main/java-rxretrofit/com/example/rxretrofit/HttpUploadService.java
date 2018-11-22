package com.example.rxretrofit;

import com.common.rxretrofit.Api.BaseResultEntity;
import com.example.rxretrofit.entity.resulte.UploadResulte;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * 测试接口service-上传相关
 * Created by WZG on 2016/12/19.
 */

public interface HttpUploadService {
    /*上传文件*/
    @Multipart
    @POST("AppYuFaKu/uploadHeadImg")
    Observable<BaseResultEntity<UploadResulte>> uploadImage(@Part("uid") RequestBody uid, @Part("auth_key") RequestBody auth_key,
                                                            @Part MultipartBody.Part file);
}
