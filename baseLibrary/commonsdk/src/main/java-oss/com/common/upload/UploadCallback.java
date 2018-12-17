package com.common.upload;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

public interface UploadCallback {
    void onProgress(long currentSize, long totalSize);

    void onSuccess(String url);

    void onFailure(String msg);
}
