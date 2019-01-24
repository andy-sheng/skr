package com.common.core.scheme.processor;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.common.base.BaseActivity;
import com.common.core.scheme.SchemeConstants;
import com.common.log.MyLog;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description Walilive的Uri的逻辑代码
 */
public class InframeProcessor implements ISchemeProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + "InframeProcessor";

    public static final String SHARE = "share";

    @Override
    public boolean accept(Uri uri) {
        String scheme = uri.getScheme();
        MyLog.w(TAG, "process scheme=" + scheme);
        if (TextUtils.isEmpty(scheme)) {
            return false;
        }

        final String host = uri.getHost();
        MyLog.w(TAG, "process host=" + host);
        if (TextUtils.isEmpty(host)) {
            return false;
        }

        if (SchemeConstants.SCHEME_INFRAMESKER.equals(scheme)) {
            return true;
        }

        return false;
    }

    public boolean process(@NonNull Uri uri, @NonNull BaseActivity activity) {
        final String host = uri.getHost();
        MyLog.w(TAG, "process host=" + host);
        if (TextUtils.isEmpty(host)) {
            return false;
        }

        MyLog.d(TAG, "process host=" + host);
        switch (host) {
            case SHARE:
                processShareUrl(uri);
                return true;
        }

        activity.finish();
        return false;
    }

    private void processShareUrl(Uri uri){

    }
}
