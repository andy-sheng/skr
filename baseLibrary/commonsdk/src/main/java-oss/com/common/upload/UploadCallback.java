package com.common.upload;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

/**
 * 注意！！！
 * 这些方法都不一定在主线程执行
 */
public interface UploadCallback {

    void onProgressNotInUiThread(long currentSize, long totalSize);

    void onSuccessNotInUiThread(String url);

    void onFailureNotInUiThread(String msg);
}
