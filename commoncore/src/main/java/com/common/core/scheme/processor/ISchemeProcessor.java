package com.common.core.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.common.base.BaseActivity;

public interface ISchemeProcessor {
    /**
     * @param uri
     * @param activity
     * @return 有没有解析成功
     */
    boolean process(@NonNull Uri uri, @NonNull Activity activity);

    /**
     *
     * @param uri
     * @return 接不接受这个host的url
     */
    boolean accept(Uri uri);
}
