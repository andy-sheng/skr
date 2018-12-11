package com.common.upload;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiObserver;
import com.common.utils.U;

import java.io.IOException;
import java.util.HashMap;

import io.reactivex.schedulers.Schedulers;

public class UploadTask {

    public final static String TAG = "UploadManager";

    static {
        if (MyLog.isDebugLogOpen()) {
            OSSLog.enableLog();  //调用此方法即可开启日志
        }
    }

    private UploadParams mUploadParams;
    private OSS mOss;
    private OSSAsyncTask mTask;

    public UploadTask(UploadParams params) {
        this.mUploadParams = params;
    }

    public UploadTask startUpload(UploadCallback uploadCallback) {
        // 在移动端建议使用STS的方式初始化OSSClient，更多信息参考：[访问控制]
        UploadAppServerApi uploadAppServerApi = ApiManager.getInstance().createService(UploadAppServerApi.class);
        uploadAppServerApi.getSTSToken().subscribeOn(Schedulers.io())
                .subscribe(new ApiObserver<JSONObject>() {

                    @Override
                    public void process(JSONObject data) {
                        int code = data.getInteger("StatusCode");
                        if (code == 200) {
                            String accessKeyId = data.getString("AccessKeyId");
                            String accessKeySecret = data.getString("accessKeySecret");
                            String securityToken = data.getString("SecurityToken");
                            OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);

                            ClientConfiguration conf = new ClientConfiguration();
                            conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
                            conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
                            conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
                            conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次

                            JSONObject uploadParams = data.getJSONObject("uploadParams");
                            String endpoint = uploadParams.getString("endpoint");

                            mOss = new OSSClient(U.app(), endpoint, credentialProvider, conf);

                            String bucketName = uploadParams.getString("bucketName");

                            JSONObject callback = uploadParams.getJSONObject("callback");
                            String callbackUrl = callback.getString("callbackUrl");
                            String callbackBody = callback.getString("callbackBody");
                            String callbackBodyType = callback.getString("callbackBodyType");

                            PutObjectRequest request = createRequest(bucketName, "1111", callbackUrl, callbackBody, callbackBodyType);

                            request.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                                @Override
                                public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                                    if (uploadCallback != null) {
                                        uploadCallback.onProgress(request, currentSize, totalSize);
                                    }
                                }
                            });

                            if (mTask != null) {
                                mTask.cancel();
                            }
                            mTask = mOss.asyncPutObject(request, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                                @Override
                                public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                                    if (uploadCallback != null) {
                                        uploadCallback.onSuccess(request, result);
                                        // 只有设置了servercallback，这个值才有数据
                                        String serverCallbackReturnJson = result.getServerCallbackReturnBody();
                                    }
                                }

                                @Override
                                public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                                    if (uploadCallback != null) {
                                        uploadCallback.onFailure(request, clientException, serviceException);
                                    }
                                }
                            });
                        }
                    }
                });
        return this;
    }

    public void cancel() {
        MyLog.d(TAG, "cancel");
        if (mTask != null) {
            mTask.cancel();
        }
    }

    /**
     * 创建普通上传
     *
     * @return
     */
    private PutObjectRequest createRequest(String buckName, String objectkey, String callbackUrl, String callbackBody, String callbackBodyType) {
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(buckName, objectkey, mUploadParams.getFilePath());

// 文件元信息的设置是可选的
        ObjectMetadata metadata = new ObjectMetadata();
        /**
         * 在Web服务中Content-Type用来设定文件的类型，决定以什么形式、什么编码读取这个文件。
         * 某些情况下，对于上传的文件需要设定Content-Type，否则文件不能以自己需要的形式和编码来读取。
         * 使用SDK上传文件时，如果不指定Content-Type，SDK会帮您根据后缀自动添加Content-Type。
         */
//       metadata.setContentType("application/octet-stream"); // 设置content-type
        try {
            metadata.setContentMD5(BinaryUtil.calculateBase64Md5(mUploadParams.getFilePath())); // 校验MD5
        } catch (IOException e) {
            e.printStackTrace();
        }
        put.setMetadata(metadata);

        put.setCallbackParam(new HashMap<String, String>() {
            {
                put("callbackUrl", callbackUrl);
//                put("callbackHost", "oss-cn-hangzhou.aliyuncs.com");
                put("callbackBodyType", callbackBodyType);
                put("callbackBody", callbackBody);
            }
        });
//        put.setCallbackVars(new HashMap<String, String>() {
//            {
//                put("x:var1", "value1");
//                put("x:var2", "value2");
//            }
//        });
        return put;
    }

//    /**
//     * 创建追加上传
//     */
//    private AppendObjectRequest createAppendRequest(){
//        AppendObjectRequest append = new AppendObjectRequest();
//
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType("application/octet-stream");
//        append.setMetadata(metadata);
//
//        // 设置追加位置
//        append.setPosition(0);
//
//        append.setProgressCallback(new OSSProgressCallback<AppendObjectRequest>() {
//            @Override
//            public void onProgress(AppendObjectRequest request, long currentSize, long totalSize) {
//            }
//        });
//        return append;
//    }

}
