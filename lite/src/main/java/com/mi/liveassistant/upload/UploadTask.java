package com.mi.liveassistant.upload;

import android.os.AsyncTask;

import com.ksyun.ks3.exception.Ks3Error;
import com.mi.liveassistant.attachment.Attachment;
import com.mi.liveassistant.common.callback.ITaskCallBack;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.task.AsyncTaskUtils;

import org.apache.http.Header;

/**
 * Created by lan on 15-11-12.
 */
public class UploadTask {
    private static final String TAG = "UploadTask";

    /**
     * 上传文件时，并判断是否需要，通知进度
     */
    public static void uploadPhoto(final Attachment att, final int authType, final ITaskCallBack callBack) {
        MyLog.w(TAG, "uploadPhoto start localPath = " + att.getLocalPath());

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {

                boolean result = UploadFileLoader.getInstance().startUploadFile(att, new UploadCallBack(att) {
                    @Override
                    public void onTaskFailure(int i, Ks3Error ks3Error, Header[] headers,
                                              String s, Throwable throwable, StringBuffer var4) {
                        MyLog.w(TAG, "onTaskFailure localPath = " + att.getLocalPath() + var4 + " ks3Error =" + ks3Error.toString()
                                + " errCode =" + ks3Error.getErrorCode() + " errMsg =" + ks3Error.getErrorMessage());
                        if (callBack != null) {
                            callBack.process(false);
                        }
                    }

                    @Override
                    public void onTaskSuccess(int i, Header[] headers, StringBuffer var4) {
                        MyLog.w(TAG, "onTaskSuccess localPath = " + att.getLocalPath() + var4);
                        if (callBack != null) {
                            callBack.process(true);
                            callBack.processWithMore(true, att.getUrl());
                        }
                    }

                    @Override
                    public void onTaskProgress(double v) {
                        MyLog.w(TAG, "onTaskProgress =" + v);
                        super.onTaskProgress(v);
                    }
                }, authType);
                return result;
            }

            @Override
            protected void onPreExecute() {
                if (callBack != null) {
                    callBack.startProcess();
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result && callBack != null) {
                    callBack.process(false);
                }
            }
        };
        AsyncTaskUtils.exeNetWorkTask(task);
    }
}
