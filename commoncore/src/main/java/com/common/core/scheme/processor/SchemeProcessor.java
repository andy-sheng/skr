package com.common.core.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.common.base.BaseActivity;
import com.common.core.scheme.SchemeConstants;
import com.common.log.MyLog;


/**
 * 以后有别的业务需要用再扩充
 */
public class SchemeProcessor implements ISchemeProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + SchemeProcessor.class.getSimpleName();

    public boolean process(@NonNull Uri uri, @NonNull Activity activity) {
        MyLog.w(TAG, "" + uri);
        return false;
    }

    @Override
    public boolean accept(Uri uri) {
        MyLog.w(TAG, "" + uri);
        return false;
    }
}
