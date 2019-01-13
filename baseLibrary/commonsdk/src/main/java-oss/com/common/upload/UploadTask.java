package com.common.upload;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
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
import com.common.utils.U;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class UploadTask {

    public final static String TAG = "UploadManager";

    static {
        if (MyLog.isDebugLogOpen()) {
            OSSLog.enableLog();  //调用此方法即可开启日志
        }
    }

    private UploadParams mUploadParams;
    private String mBucketName;
    private String mDir = "";
    private String mCallbackUrl;
    private String mCallbackBody;
    private String mCallbackBodyType;
    private OSS mOss;
    private OSSAsyncTask mTask;

    public UploadTask(UploadParams params) {
        this.mUploadParams = params;
    }

    public UploadTask startUpload(UploadCallback uploadCallback) {
        File file = new File(mUploadParams.getFilePath());
        if (file != null && file.exists()) {
            MyLog.w(TAG, "startUpload fileLength:" + file.length());
        } else {
            MyLog.e(TAG, "file==null 或者 文件不存在");
            return this;
        }
        // 在移动端建议使用STS的方式初始化OSSClient，更多信息参考：[访问控制]
        UploadAppServerApi uploadAppServerApi = ApiManager.getInstance().createService(UploadAppServerApi.class);
        uploadAppServerApi.getSTSToken(mUploadParams.getFileType().getOssSavaDir()).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<JSONObject>() {
                    @Override
                    public void accept(JSONObject data) throws Exception {
                        boolean has = data.containsKey("statusCode");
                        if (!has) {
                            return;
                        }
                        int code = data.getInteger("statusCode");
                        if (code == 200) {
                            String accessKeyId = data.getString("accessKeyId");
                            String accessKeySecret = data.getString("accessKeySecret");
                            String securityToken = data.getString("securityToken");
                            OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);

                            ClientConfiguration conf = new ClientConfiguration();
                            conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
                            conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
                            conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
                            conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次

                            JSONObject uploadParams = data.getJSONObject("uploadParams");
                            String endpoint = uploadParams.getString("endpoint");

                            mOss = new OSSClient(U.app(), endpoint, credentialProvider, conf);

                            mBucketName = uploadParams.getString("bucketName");
                            mDir = uploadParams.getString("dir");

                            JSONObject callback = uploadParams.getJSONObject("callback");
                            mCallbackUrl = callback.getString("callbackUrl");
                            mCallbackBody = callback.getString("callbackBody");
                            mCallbackBodyType = callback.getString("callbackBodyType");

                            if (mUploadParams.isNeedCompress()) {
                                String fileName = U.getFileUtils().getFileNameFromFilePath(mUploadParams.getFilePath());
                                String targetFileName = U.getAppInfoUtils().getFilePathInSubDir("upload", "temp_" + fileName);
                                // 需要压缩
                                Luban.with(U.app())
                                        .load(mUploadParams.getFilePath())
                                        .ignoreBy(100)
                                        .setTargetDir(targetFileName)
                                        .filter(new CompressionPredicate() {
                                            @Override
                                            public boolean apply(String path) {
                                                if (path.toLowerCase().endsWith(".gif")) {
                                                    return false;
                                                }
                                                if (path.toLowerCase().endsWith(".zip")) {
                                                    return false;
                                                }
                                                return true;
                                            }
                                        })
                                        .setCompressListener(new OnCompressListener() {
                                            @Override
                                            public void onStart() {

                                            }

                                            @Override
                                            public void onSuccess(File file) {
                                                MyLog.d(TAG, "压缩成功" + " file=" + file.getAbsolutePath());
                                                upload(file.getAbsolutePath(), uploadCallback);
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                MyLog.d(TAG, "压缩失败");
                                                upload(mUploadParams.getFilePath(), uploadCallback);
                                            }
                                        }).launch();
                            } else {
                                upload(mUploadParams.getFilePath(), uploadCallback);
                            }

                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MyLog.e(throwable);
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

    private void upload(String filePath, UploadCallback uploadCallback) {
        PutObjectRequest request = createRequest(filePath);

        request.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                if (uploadCallback != null) {
                    uploadCallback.onProgress(currentSize, totalSize);
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
                    // 只有设置了servercallback，这个值才有数据
                    String serverCallbackReturnJson = result.getServerCallbackReturnBody();
                    MyLog.w(TAG, "serverCallbackReturnJson:" + serverCallbackReturnJson);
                    JSONObject jo = JSON.parseObject(serverCallbackReturnJson);
                    uploadCallback.onSuccess(jo.getString("url"));
                }
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                if (uploadCallback != null) {
                    uploadCallback.onFailure("error:" + serviceException.getErrorCode() + " clientMsg:" + clientException.getMessage() + " serverMsg:" + serviceException.getMessage());
                }
            }
        });
    }

    /**
     * 创建普通上传
     *
     * @return
     */
    private PutObjectRequest createRequest(String filePath) {
        String mObjectId;
        if (mDir.length() > 0 && !mDir.endsWith("/")) {
            mDir += "/";
        }
        if (TextUtils.isEmpty(mUploadParams.getFileName())) {
            String ext = U.getFileUtils().getSuffixFromFilePath(filePath);
            String fileName = U.getMD5Utils().MD5_16(System.currentTimeMillis() + filePath);
            if (TextUtils.isEmpty(mDir)) {
                mObjectId = mUploadParams.getFileType().getOssSavaDir() + fileName + "." + ext;
            } else {
                mObjectId = mDir + fileName + "." + ext;
            }
        } else {
            mObjectId = mDir + mUploadParams.getFileName();
        }
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(mBucketName, mObjectId, filePath);

// 文件元信息的设置是可选的
        ObjectMetadata metadata = new ObjectMetadata();
        /**
         * 在Web服务中Content-Type用来设定文件的类型，决定以什么形式、什么编码读取这个文件。
         * 某些情况下，对于上传的文件需要设定Content-Type，否则文件不能以自己需要的形式和编码来读取。
         * 使用SDK上传文件时，如果不指定Content-Type，SDK会帮您根据后缀自动添加Content-Type。
         */
//       metadata.setContentType("application/octet-stream"); // 设置content-type
        try {
            metadata.setContentMD5(BinaryUtil.calculateBase64Md5(filePath)); // 校验MD5
        } catch (IOException e) {
            e.printStackTrace();
        }
        put.setMetadata(metadata);

        put.setCallbackParam(new HashMap<String, String>() {
            {
                put("callbackUrl", mCallbackUrl);
//                put("callbackHost", "oss-cn-hangzhou.aliyuncs.com");
                put("callbackBodyType", mCallbackBodyType);
                put("callbackBody", mCallbackBody);
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
