package com.common.webview.aidl;

import android.os.RemoteException;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.WebIpcCallback;
import com.common.core.WebIpcService;
import com.common.log.MyLog;
import com.common.rxretrofit.cookie.persistence.SharedPrefsCookiePersistor;
import com.common.utils.U;

import java.util.List;

import okhttp3.Cookie;


public class WebIpcServer extends com.common.core.WebIpcService.Stub {

    public final static String TAG = "WebIpcServer";

    public static int TYPE_GET_COOKIES = 1;

    @Override
    public void call(int type, String json, WebIpcCallback callback) throws RemoteException {
        MyLog.w(TAG, "call" + " type=" + type + " json=" + json + " callback=" + callback);
        if (type == TYPE_GET_COOKIES) {
            if (callback != null) {
                JSONArray jsonArray = new JSONArray();
//                jsonObject.
                //种cookie，先注释掉
                SharedPrefsCookiePersistor mSharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(U.app());
                List<Cookie> cookies = mSharedPrefsCookiePersistor.loadAll();
                if (cookies != null && cookies.size() > 0) {
                    for (Cookie cookie : cookies) {
                        jsonArray.add(cookie.toString());
                    }
                }
                callback.callback(jsonArray.toJSONString());
            }
        }
    }

}
