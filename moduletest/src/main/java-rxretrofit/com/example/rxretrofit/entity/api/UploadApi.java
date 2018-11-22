package com.example.rxretrofit.entity.api;

import com.common.rxretrofit.Api.BaseApi;
import com.common.rxretrofit.Api.BaseResultEntity;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.upload.ProgressRequestBody;
import com.common.rxretrofit.upload.UploadProgressListener;
import com.example.rxretrofit.HttpUploadService;
import com.example.rxretrofit.entity.resulte.UploadResulte;

import java.io.File;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 上传请求api
 * Created by WZG on 2016/10/20.
 */

public class UploadApi extends BaseApi {
    /*需要上传的文件*/
    private MultipartBody.Part part;

    public UploadApi() {
        super();
        setCache(false);
    }

    public Observable<BaseResultEntity<UploadResulte>> uploadImage(long uid, String key, String path, String mediaType) {
        File file = new File(path);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part part1 = MultipartBody.Part.createFormData("file_name", file.getName(), new ProgressRequestBody(
                requestBody, new UploadProgressListener() {
            @Override
            public void onProgress(long currentBytesCount, long totalBytesCount) {

            }
        }));

        HttpUploadService service = ApiManager.getInstance().newClient(this).create(HttpUploadService.class);
        RequestBody uidBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(uid));
        RequestBody keyBody = RequestBody.create(MediaType.parse("text/plain"), key);
        return service.uploadImage(uidBody, keyBody, part1);
    }
}
