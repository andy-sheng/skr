package com.common.webview.aidl;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


public class WebIpcProvider extends ContentProvider {
    public final String TAG = "WebviewIpcServer";

    public final static String AUTHORITY = "com.zq.live.web.binder.service";

    public final static String WEBVIEW_SERVER = "webview_server";
    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        IBinder binder;

        if (selectionArgs[0].equals(WEBVIEW_SERVER)) {
            binder = new WebIpcServer();

            Log.d(TAG, "Query OrderServiceImpl");
        } else {
            return null;
        }

        BinderCursor cursor = new BinderCursor(new String[]{"service"}, binder);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[]
            selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String
            selection, @Nullable String[] selectionArgs) {

        return 0;
    }
}
