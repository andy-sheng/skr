package com.wali.live.watchsdk.webview;

import android.webkit.WebView;

import org.json.JSONObject;

/**
 * Created by yangli on 16-6-21.
 */
public class JsBridgeProtocol {
    public static final String FUNC = "func";
    public static final String PARAMS = "params";
    public static final String MSG_TYPE = "__msg_type";
    public static final String EVENT_ID = "__event_id";

    public static final String CALLBACK_PARAMS = "__params";
    public static final String CALLBACK_ID = "__callback_id";

    public static final String MSG_TYPE_CALLBACK = "callback";
    public static final String MSG_TYPE_EVENT = "event";

    public static final String EXTRA_KEY_URL = "url";
    public static final String EXTRA_KEY_RESULT = "result";

    public interface IJsBridge {
        void onResult(WebView webView, JSONObject result);
    }
}
