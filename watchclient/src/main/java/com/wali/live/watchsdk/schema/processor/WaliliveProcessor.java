package com.wali.live.watchsdk.schema.processor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.wali.live.watchsdk.schema.SchemeConstants;
import com.wali.live.watchsdk.schema.processor.complex.RoomProcessor;
import com.wali.live.watchsdk.webview.HalfWebViewActivity;
import com.wali.live.watchsdk.webview.WebViewActivity;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description Walilive的Uri的逻辑代码
 */
public class WaliliveProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + WaliliveProcessor.class.getSimpleName();

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
                RoomProcessor.processHostRoom(uri, activity, finishActivity);
                return true;
            case SchemeConstants.HOST_OPEN_URL:
                processHostOpenUrl(uri, activity);
                break;
            default:
                return false;
        }
        if (finishActivity) {
            activity.finish();
        }
        return true;
    }

    public static boolean isLegalPath(Uri uri, String logKey, @NonNull String comparePath) {
        MyLog.d(TAG, logKey + " uri=" + uri);
        if (uri == null) {
            return false;
        }
        String path = uri.getPath();
        return comparePath.equals(path);
    }

    public static void processHostOpenUrl(Uri uri, @NonNull Activity activity) {
        if (!isLegalPath(uri, "processHostOpenUrl", SchemeConstants.PATH_WINDOW_TYPE)) {
            return;
        }

        String url = Uri.decode(uri.getQueryParameter(SchemeConstants.PARAM_URI_WEBVIEW_RUL));
        boolean isHalf = uri.getBooleanQueryParameter(SchemeConstants.PARAM_URI_WEBVIEW_ISHALF, false);

        Intent intent = new Intent(activity, isHalf ? HalfWebViewActivity.class : WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url);
        intent.putExtra(WebViewActivity.EXTRA_DISPLAY_TYPE, isHalf);
        activity.startActivity(intent);
    }
}
