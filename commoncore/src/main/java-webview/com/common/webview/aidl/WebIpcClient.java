package com.common.webview.aidl;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import com.common.core.WebIpcService;
import com.common.utils.U;

public class WebIpcClient {
    public static void  getServer(GetCallback callback){
        final ContentResolver resolver = U.app().getContentResolver();
        final Cursor cu = resolver.query(Uri.parse("content://" + WebIpcProvider.AUTHORITY + "/binder"), null, null, new String[]{WebIpcProvider.WEBVIEW_SERVER}, null);
        if (cu == null) {
            return ;
        }
        IBinder binder = getBinder(cu);
        WebIpcService webIpcService =  WebIpcService.Stub.asInterface(binder);
        if (callback != null) {
            callback.get(webIpcService);
        }
        cu.close();
    }

    private static final IBinder getBinder(Cursor cursor) {
        Bundle extras = cursor.getExtras();
        extras.setClassLoader(BinderCursor.BinderParcelable.class.getClassLoader());
        BinderCursor.BinderParcelable w = extras.getParcelable("binder");
        return w.mBinder;
    }

    public interface GetCallback{
        void get(WebIpcService webIpcService);
    }
}
