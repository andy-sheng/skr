package com.wali.live.livesdk.live.upload;

import com.base.log.MyLog;
import com.ksyun.ks3.exception.Ks3Error;
import com.ksyun.ks3.model.result.CompleteMultipartUploadResult;
import com.ksyun.ks3.services.handler.PutObjectResponseHandler;
import com.mi.live.data.assist.Attachment;

import org.apache.http.Header;

public abstract class UploadCallBack extends PutObjectResponseHandler {
    private static final String TAG = UploadCallBack.class.getSimpleName();

    private Attachment mAtt;

    public UploadCallBack(Attachment att) {
        this.mAtt = att;
    }

    public String onPostExecute(final boolean result, final String downloadUrl, final String resId) {
        if (result) {
            mAtt.setResourceId(resId);
        } else {
            MyLog.w(TAG, "upload file failed, local path = " + mAtt.getLocalPath());
        }
        return downloadUrl;
    }

    @Override
    public abstract void onTaskFailure(int i, Ks3Error ks3Error, Header[] headers, String s, Throwable throwable, StringBuffer var4);

    @Override
    public abstract void onTaskSuccess(int i, Header[] headers, StringBuffer var4);

    public void onTaskSuccess(int i, Header[] headers, CompleteMultipartUploadResult result, StringBuffer var4) {
        onTaskSuccess(i, headers,  var4);
    }

    @Override
    public void onTaskStart() {
    }

    @Override
    public void onTaskFinish() {
    }

    @Override
    public void onTaskCancel() {
    }

    @Override
    public void onTaskProgress(double v) {
    }
}
