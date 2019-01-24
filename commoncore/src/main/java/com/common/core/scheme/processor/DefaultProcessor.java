package com.common.core.scheme.processor;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.common.base.BaseActivity;
import com.common.core.scheme.SchemeConstants;
import com.common.log.MyLog;

public class DefaultProcessor implements ISchemeProcessor {
    public final static String TAG = SchemeConstants.LOG_PREFIX + "DefaultProcessor";

    @Override
    public boolean process(@NonNull Uri uri, @NonNull BaseActivity activity) {
        MyLog.w(TAG, "" + uri);
        activity.finish();
        return true;
    }

    @Override
    public boolean accept(Uri uri) {
        MyLog.w(TAG, "" + uri);
        return true;
    }
}
