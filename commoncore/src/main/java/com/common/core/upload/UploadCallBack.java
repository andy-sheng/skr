package com.common.core.upload;

/**
 * 上传回调
 */
public abstract class UploadCallBack {

    public abstract void onTaskStart();

    public abstract void onTaskCancel();

    public abstract void onTaskProgress(double progress);

    public abstract void onTaskFailure();

    public abstract void onTaskSuccess();
}
