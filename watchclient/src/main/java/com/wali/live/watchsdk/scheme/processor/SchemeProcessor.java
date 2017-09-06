package com.wali.live.watchsdk.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.wali.live.watchsdk.scheme.SchemeConstants;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description LiveSdk的Uri的逻辑代码
 */
public class SchemeProcessor extends CommonProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + SchemeProcessor.class.getSimpleName();

    public static boolean process(@NonNull Uri uri, String host, @NonNull RxActivity activity, boolean finishActivity) {
        if (TextUtils.isEmpty(host)) {
            host = uri.getHost();
            if (TextUtils.isEmpty(host)) {
                MyLog.d(TAG, "process host is empty, uri=" + uri);
                return false;
            }
        }
        MyLog.d(TAG, "process host=" + host);
        switch (host) {
            case SchemeConstants.HOST_ROOM:
                processHostRoom(uri, activity);
                break;
            case SchemeConstants.HOST_PLAYBACK:
                processHostPlayback(uri, activity);
                break;
            case SchemeConstants.HOST_CHANNEL:
                processHostChannel(uri, activity);
                break;
            case SchemeConstants.HOST_ZHIBO_COM:
                processHostLivesdk(uri, activity);
                break;
            default:
                return false;
        }
        if (finishActivity) {
            activity.finish();
        }
        return true;
    }

    /**
     * 使用action模拟正常livesdk的path
     */
    private static void processHostLivesdk(Uri uri, @NonNull Activity activity) {
        String action = uri.getQueryParameter(SchemeConstants.PARAM_ACTION);
        switch (action) {
            case SchemeConstants.HOST_ROOM:
                processHostRoom(uri, activity);
                break;
            case SchemeConstants.HOST_PLAYBACK:
                processHostPlayback(uri, activity);
                break;
            case SchemeConstants.HOST_CHANNEL:
                processHostChannel(uri, activity);
                break;
        }
    }
}
