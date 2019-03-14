package com.common.upload;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

/**
 * 这些方法都不一定在主线程执行
 */
public interface UploadCallback {

    void onProgress(long currentSize, long totalSize);

    void onSuccess(String url);

    void onFailure(String msg);
}
