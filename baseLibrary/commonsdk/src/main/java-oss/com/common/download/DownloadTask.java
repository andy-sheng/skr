package com.common.download;

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
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.utils.HttpUtils;
import com.common.utils.U;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class DownloadTask {

    public final static String TAG = "UploadManager";

    static {
        if (MyLog.isDebugLogOpen()) {
            OSSLog.enableLog();  //调用此方法即可开启日志
        }
    }

    public interface OssDownloadAdapter{
        boolean isCancel();
        void onGetResult(InputStream objectContent, long contentLength);
    }

    OssDownloadAdapter mOssDownloadAdapter;
    private String mBucketName;
    private String mObjectId;
    private OSS mOss;
    private OSSAsyncTask mTask;

    public DownloadTask(String bucketName, String objectId,OssDownloadAdapter ossDownloadAdapter) {
        this.mBucketName = bucketName;
        this.mObjectId = objectId;
        this.mOssDownloadAdapter = ossDownloadAdapter;
    }

    public DownloadTask downloadAsync() {
        // 在移动端建议使用STS的方式初始化OSSClient，更多信息参考：[访问控制]
        DownloadAppServerApi downloadAppServerApi = ApiManager.getInstance().createService(DownloadAppServerApi.class);
        downloadAppServerApi.getSTSToken().subscribeOn(Schedulers.io())
                .subscribe(new Consumer<JSONObject>() {
                    @Override
                    public void accept(JSONObject data) throws Exception {
                        int code = data.getInteger("StatusCode");
                        if (code == 200) {
                            String accessKeyId = data.getString("AccessKeyId");
                            String accessKeySecret = data.getString("AccessKeySecret");
                            String securityToken = data.getString("SecurityToken");
                            OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);

                            ClientConfiguration conf = new ClientConfiguration();
                            conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
                            conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
                            conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
                            conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次


                            mOss = new OSSClient(U.app(), credentialProvider, conf);
                            download();
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

    private void download() {
        GetObjectRequest get = new GetObjectRequest(mBucketName, mObjectId);

        get.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                if (mOssDownloadAdapter != null) {
                    if(mOssDownloadAdapter.isCancel()){
                        if (mTask != null) {
                            mTask.cancel();
                        }
                    }
                }
            }
        });

        if (mTask != null) {
            mTask.cancel();
        }
        mTask = mOss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                if (mOssDownloadAdapter != null) {
                    mOssDownloadAdapter.onGetResult(result.getObjectContent(),result.getContentLength());
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientException, ServiceException serviceException) {

            }
        });
    }


}
