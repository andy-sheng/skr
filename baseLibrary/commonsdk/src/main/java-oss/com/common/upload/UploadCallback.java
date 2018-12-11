package com.common.upload;

import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

public interface UploadCallback extends OSSCompletedCallback {
    void onProgress(PutObjectRequest request, long currentSize, long totalSize);
}
