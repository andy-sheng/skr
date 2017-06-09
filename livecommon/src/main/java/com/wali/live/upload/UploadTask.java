package com.wali.live.upload;

import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.base.utils.Base64Coder;
import com.base.utils.Constants;
import com.base.utils.version.VersionManager;
import com.ksyun.ks3.exception.Ks3Error;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.assist.Attachment;
import com.wali.live.feedback.FeedBackController;
import com.wali.live.statistics.StatisticUtils;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.task.ITaskCallBack;
import com.wali.live.utils.AsyncTaskUtils;
import com.wali.live.utils.HttpUtils;
import com.xiaomi.accountsdk.request.AccessDeniedException;
import com.xiaomi.accountsdk.request.AuthenticationFailureException;
import com.xiaomi.accountsdk.request.SimpleRequest;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 15-11-12.
 */
public class UploadTask {
    private static final String TAG = "UploadTask";

    /**
     * 　上传文件时，并判断是否需要　通知进度
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
                        MyLog.v(TAG, "onTaskProgress =" + v);
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

    public static void uploadFeedBack(final Attachment att, final ITaskCallBack callBack, final String phonenumber, final String feedbackmessage) {
        MyLog.w(TAG, "uploadFeedBack start localPath = " + att.getLocalPath());
        UploadFileLoader.getInstance().startUploadFile(att, new UploadCallBack(att) {
            @Override
            public void onTaskFailure(int i, Ks3Error ks3Error, Header[] headers, String s, Throwable throwable, StringBuffer var4) {
                MyLog.w(TAG, "onTaskFailure localPath = " + att.getLocalPath());
                if (callBack != null) {
                    callBack.process(false);
                }
            }

            @Override
            public void onTaskSuccess(int i, Header[] headers, StringBuffer var4) {
                MyLog.w(TAG, "onTaskSuccess localPath = " + att.getLocalPath() + " localUrl=" + att.url);
                if (callBack != null) {
                    callBack.process(true);
                }
                ThreadPool.runOnPool(new Runnable() {
                    @Override
                    public void run() {
                        notifyServerUploadResult(phonenumber, feedbackmessage, att.url);
                    }
                });
                //new File(att.getLocalPath()).delete();
            }
        }, att.authType);
    }

    public static void notifyServerUploadResult(String phonenumber, String feedbackmessage, String logUrl) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        String vuid = UserAccountManager.getInstance().getUuid();
        nameValuePairs.add(new BasicNameValuePair("vuid", vuid));
        nameValuePairs.add(new BasicNameValuePair("imagesurls", logUrl));
        nameValuePairs.add(new BasicNameValuePair("logurl", logUrl));
        if (MyUserInfoManager.getInstance().getUser() != null) {
            nameValuePairs.add(new BasicNameValuePair("lvl", String.valueOf(MyUserInfoManager.getInstance().getUser().getLevel())));
        }
        nameValuePairs.add(new BasicNameValuePair("phonenumber", Base64Coder.encodeString(phonenumber)));
        nameValuePairs.add(new BasicNameValuePair("feedbackmessage", Base64Coder.encodeString(feedbackmessage)));
        nameValuePairs.add(new BasicNameValuePair("time", String.valueOf(System.currentTimeMillis())));
        nameValuePairs.add(new BasicNameValuePair("ua", Build.MODEL));
        nameValuePairs.add(new BasicNameValuePair("os", "android_" + android.os.Build.VERSION.RELEASE));
        nameValuePairs.add(new BasicNameValuePair("version", String.valueOf(VersionManager.getCurrentVersionCode(GlobalData.app()))));
        nameValuePairs.add(new BasicNameValuePair("appid", String.valueOf(Constants.MILINK_APP_ID)));
        SimpleRequest.StringContent result = null;
        MyLog.w(TAG, "logUrl=" + logUrl + " uid =" + vuid + " aappid=" + Constants.MILINK_APP_ID);
        try {
            MyLog.w(TAG, "VersionCheck Get Request Params : " + nameValuePairs);
            result = HttpUtils.doV2PostAsString(FeedBackController.FEED_BACK_URL, nameValuePairs);
            MyLog.w(TAG, "VersionCheck return : " + result);
        } catch (IOException e) {
            MyLog.e(e);
        } catch (AccessDeniedException e) {
            MyLog.e(e);
        } catch (AuthenticationFailureException e) {
            MyLog.e(e);
        }

        int retCode = parseResult(result);
        StatisticUtils.addToMiLinkMonitor(StatisticsKey.FEED_BACK_RESULT, retCode == FeedBackController.LOG_FILE_UPLOAD_SUCCESS ? 0 : retCode);
    }

    private static int parseResult(SimpleRequest.StringContent result) {
        if (result == null) {
            return FeedBackController.LOG_FILE_UPLOAD_FAILURE;
        }
        String jsonString = result.getBody();
        if (TextUtils.isEmpty(jsonString)) {
            return FeedBackController.LOG_FILE_UPLOAD_FAILURE;
        }
        if (jsonString.contains("ok")) {
            return FeedBackController.LOG_FILE_UPLOAD_SUCCESS;
        }
        try {
            JSONObject resultObj = new JSONObject(jsonString);
            if (!resultObj.has("body") || !"ok".equalsIgnoreCase(resultObj.getString("body"))) {
                return FeedBackController.LOG_FILE_UPLOAD_FAILURE;
            }

        } catch (JSONException e) {
            MyLog.e(e);
            return FeedBackController.LOG_FILE_UPLOAD_FAILURE;
        }
        return FeedBackController.LOG_FILE_UPLOAD_SUCCESS;
    }
}
