package com.wali.live.livesdk.live.upload;

import android.os.AsyncTask;

import com.base.log.MyLog;
import com.ksyun.ks3.exception.Ks3Error;
import com.mi.live.data.assist.Attachment;
import com.wali.live.livesdk.live.task.ITaskCallBack;
import com.wali.live.utils.AsyncTaskUtils;

import org.apache.http.Header;

/**
 * Created by lan on 15-11-12.
 */
public class UploadTask {
    private static final String TAG = "UploadTask";

    /**
     * 　上传文件时，并判断是否需要　通知进度
     */
    public static void uploadPhoto(final Attachment att, final int authType, final ITaskCallBack callBack) {
        MyLog.d(TAG, "uploadPhoto start localPath = " + att.getLocalPath());

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {

                boolean result = UploadFileLoader.getInstance().startUploadFile(att, new UploadCallBack(att) {
                    @Override
                    public void onTaskFailure(int i, Ks3Error ks3Error, Header[] headers,
                                              String s, Throwable throwable, StringBuffer var4) {
                        MyLog.d(TAG, "onTaskFailure localPath = " + att.getLocalPath() + var4 + " ks3Error =" + ks3Error.toString()
                                + " errCode =" + ks3Error.getErrorCode() + " errMsg =" + ks3Error.getErrorMessage());
                        if (callBack != null) {
                            callBack.process(false);
                        }
                    }

                    @Override
                    public void onTaskSuccess(int i, Header[] headers, StringBuffer var4) {
                        MyLog.d(TAG, "onTaskSuccess localPath = " + att.getLocalPath() + var4);
                        if (callBack != null) {
                            callBack.process(true);
                            callBack.processWithMore(true, att.getUrl());
                        }

                    }

                    @Override
                    public void onTaskProgress(double v) {
                        MyLog.d(TAG, "onTaskProgress =" + v);
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
