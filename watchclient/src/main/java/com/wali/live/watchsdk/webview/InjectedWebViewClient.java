package com.wali.live.watchsdk.webview;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;

/**
 * Created by yangli on 16-6-20.
 */
public class InjectedWebViewClient extends LiveWebViewClient {
    private static final String TAG = "InjectedWebViewClient";

    private static final int MAX_JS_FILE = 1;
    private static final String JS_BRIDGE_VERSION = "101";
    private static final String INJECTION_TOKEN = "**MiLiveInjection**";
    private static final String LOCAL_ASSET_PATH = INJECTION_TOKEN + "file:///android_asset/";

    public static final String JS_MSG_SET_RESULT = "milive://private/setresult/";
    public static final String JS_MSG_SCENE_FETCH_QUEUE = "milive://private/setresult/SCENE_FETCHQUEUE&";

    private static final String FUNC = "func";
    private static final String PARAMS = "params";
    static final String MSG_TYPE = "__msg_type";
    static final String CALLBACK_ID = "__callback_id";
    static final String EVENT_ID = "__event_id";

    static final String MSG_TYPE_CALLBACK = "callback";
    static final String MSG_TYPE_EVENT = "event";

    private BridgeHandler mBridgeHandler = new BridgeHandler();
    private JsBridgeImpl mJsBridgeImpl;

    public InjectedWebViewClient(WebViewListener mWebViewListener, BaseSdkActivity activity) {
        super(mWebViewListener, activity);
        mJsBridgeImpl = new JsBridgeImpl(mBridgeHandler, activity);
    }

    public void onDestroy() {
        if (null != mJsBridgeImpl) {
            mJsBridgeImpl.onDestroy();
        }
    }

    public void setResultToJs(final WebView webView, String result) {
        if (webView == null) {
            return;
        }
        webView.loadUrl(String.format("javascript:window.JsBridge._handleMessageFromClient('%s')",
                "" + Base64.encodeToString((result == null ? "" : result).getBytes(), Base64.DEFAULT)));
    }

    private boolean isDispatchMessageUrl(String url) {
        return "milive://dispatch_message/".equalsIgnoreCase(url);
    }

    private boolean isNeedInjectJsBridge(String url) {
        return url.contains("mi.com") || url.contains("xiaomi.com");
    }

    private void onNewRealUrl(String url) {
        mJsBridgeImpl.updateCurrUrl(url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        MyLog.d(TAG, "shouldOverrideUrlLoading url=" + url);
        if (isDispatchMessageUrl(url)) {
            Message msg = mBridgeHandler.obtainMessage(
                    BridgeHandler.MSG_FETCH_MESSAGE, view);
            mBridgeHandler.sendMessage(msg);
            return true;
        } else if (url.startsWith(JS_MSG_SCENE_FETCH_QUEUE)) {
            int pos = url.indexOf(JS_MSG_SCENE_FETCH_QUEUE) + JS_MSG_SCENE_FETCH_QUEUE.length();
            Message msg = mBridgeHandler.obtainMessage(
                    BridgeHandler.MSG_PROCESS_MESSAGE,
                    view);
            Bundle data = msg.getData();
            data.putString(JsBridgeProtocol.EXTRA_KEY_URL, url.substring(pos));
            mBridgeHandler.sendMessage(msg);
            return true;
        } else if (url.startsWith(JS_MSG_SET_RESULT)) {
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    public void shareCallBack(int status, String type) {
        mJsBridgeImpl.shareCallBack(status, type);
    }

    public void goBack(WebView webView) {
        mJsBridgeImpl.goBackIndex(webView);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        MyLog.w(TAG, "shouldInterceptRequest url=" + url);
        WebResourceResponse response = super.shouldInterceptRequest(view, url);
        if (url != null && url.contains(INJECTION_TOKEN)
                && url.contains(LOCAL_ASSET_PATH)) {
            String assetPath = url.substring(url.indexOf(LOCAL_ASSET_PATH)
                    + LOCAL_ASSET_PATH.length(), url.length());
            try {
                response = new WebResourceResponse("application/javascript",
                        "UTF8", view.getContext().getAssets().open(assetPath));
            } catch (Exception e) {
                MyLog.e(TAG, "shouldInterceptRequest exception=" + e);
            }
        }
        return response;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        MyLog.w(TAG, "onPageFinished url=" + url);
        super.onPageFinished(view, url);
        if (!TextUtils.isEmpty(url)) { // 有效地址才注入JSBridget
            try {
                String realUrl = URLDecoder.decode(url, "UTF-8");
                MyLog.d(TAG, "onPageFinished realUrl=" + realUrl);
                onNewRealUrl(realUrl);
                if (isNeedInjectJsBridge(realUrl.toLowerCase())) {
                    webViewLoadJs(view, INJECTION_TOKEN + "file:///android_asset/JsBridge.js", 0);
                }
                webViewLoadShareJs(view);
            } catch (Exception e) {
                MyLog.e(TAG, "onPageFinished exception=" + e);
            } catch (NoClassDefFoundError e) {
                MyLog.e(TAG, "onPageFinished exception=" + e);
            }
        }
    }

    private static void webViewLoadJs(WebView webView, String url, int loadIndex) {
        StringBuilder js = new StringBuilder();
        js.append("var newscript = document.createElement(\"script\");");
        js.append("newscript.src=\"").append(url).append("\";");
        if (MAX_JS_FILE - 1 == loadIndex) {
            js.append("newscript.onload=function(){window.JsBridge._init(");
            JSONObject versionJs = new JSONObject();
            try {
                versionJs.put("version", JS_BRIDGE_VERSION);
                js.append(versionJs.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            js.append(");};");
        }
        js.append("document.body.appendChild(newscript);");
        webView.loadUrl("javascript:" + js.toString());
    }

    private static void webViewLoadShareJs(WebView webView) {
        MyLog.w(TAG, "webViewLoadShareJs");
        StringBuilder js = new StringBuilder();
        js.append("var newscript = document.createElement(\"script\");");
        js.append("newscript.text = window.JavaScriptInterface.command(JSON.stringify(window.h5share));");
        js.append("document.body.appendChild(newscript);");
        webView.loadUrl("javascript:" + js.toString());
    }

    /**
     * 刷新页面数据 客户端调用
     *
     * @param webView
     */
    public void refresh(WebView webView) {
        //未注入的时候 不能使用JSBridge
        if (null == webView) {
            return;
        }
        JSONObject outParams = new JSONObject();
        try {
            outParams.put(MSG_TYPE, MSG_TYPE_EVENT);
            outParams.put(EVENT_ID, "sys:refresh");
        } catch (Exception e) {
            //do nothing
        }
        setResultToJs(webView, outParams.toString());
    }

    @SuppressLint("HandlerLeak")
    protected final class BridgeHandler extends Handler {
        public static final int MSG_FETCH_MESSAGE = 0x100;
        public static final int MSG_PROCESS_MESSAGE = 0x101;
        public static final int MSG_SET_RESULT_TO_JS = 0x102;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null == msg) {
                return;
            }
            switch (msg.what) {
                case MSG_FETCH_MESSAGE: {
                    final WebView webView = (WebView) msg.obj;
                    if (webView != null) {
                        // 这个延迟是必须的 XXX  TODO: use RxJava to do this
                        ThreadPool.getUiHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 需要一个队列来处理事件
                                MyLog.w(TAG, "load:javascript:JsBridge._fetchQueue();");
                                if (webView != null) { // 保护下
                                    try {
                                        webView.loadUrl("javascript:JsBridge._fetchQueue();"); // 记得要有括号与分号，否则无法调用起来
                                    } catch (Exception e) {
                                        MyLog.w("", "", e);
                                    } catch (NoClassDefFoundError e) {
                                        MyLog.e("", "", e);
                                    }
                                }
                            }
                        }, 50);
                    }
                    break;
                }
                case MSG_PROCESS_MESSAGE: {
                    WebView webView = (WebView) msg.obj;
                    Bundle data = msg.getData();
                    String url = data.getString(JsBridgeProtocol.EXTRA_KEY_URL);
                    try {
                        String realMsg = new String(Base64.decode(url, Base64.DEFAULT));
                        JSONArray js = new JSONArray(realMsg);
                        MyLog.w(TAG, "js=" + js.toString());
                        final int count = js.length();
                        JSONObject jsObj;
                        String funcName;
                        JSONObject paramsJS;
                        String msgType;
                        String callBackId;
                        String eventId;

                        Class<?> cls = JsBridgeImpl.class;
                        Method localMethod;
                        for (int i = 0; i < count; i++) {
                            jsObj = js.getJSONObject(i);
                            MyLog.w(TAG, "jsObj=" + jsObj.toString());

                            msgType = jsObj.optString(JsBridgeProtocol.MSG_TYPE);
                            MyLog.w(TAG, "msgType=" + msgType);

                            funcName = jsObj.getString(JsBridgeProtocol.FUNC);

                            paramsJS = jsObj.getJSONObject(JsBridgeProtocol.PARAMS);
                            Class<?>[] arrayOfClass = new Class[4];
                            arrayOfClass[0] = WebView.class;
                            arrayOfClass[1] = String.class;
                            arrayOfClass[2] = String.class;
                            arrayOfClass[3] = JSONObject.class;

                            Object[] arrayOfObject = new Object[4];
                            arrayOfObject[0] = webView;
                            arrayOfObject[3] = paramsJS;
                            try {
                                localMethod = cls.getMethod(funcName, arrayOfClass);
                                localMethod.setAccessible(true);
                                if ("call".equalsIgnoreCase(msgType)) {
                                    arrayOfObject[1] = JsBridgeProtocol.MSG_TYPE_CALLBACK;
                                    callBackId = jsObj.optString(JsBridgeProtocol.CALLBACK_ID);
                                    MyLog.w(TAG, "callbackId=" + callBackId);
                                    arrayOfObject[2] = callBackId;
                                } else if ("event".equalsIgnoreCase(msgType)) {
                                    arrayOfObject[1] = JsBridgeProtocol.MSG_TYPE_CALLBACK;
                                    eventId = jsObj.optString(JsBridgeProtocol.EVENT_ID);
                                    MyLog.w(TAG, "eventId=" + eventId);
                                    arrayOfObject[2] = eventId;
                                } else {
                                    continue;
                                }
                                localMethod.invoke(InjectedWebViewClient.this.mJsBridgeImpl, arrayOfObject);
                            } catch (NoSuchMethodException e) {
                                MyLog.w("", "", e);
                            } catch (IllegalAccessException e) {
                                MyLog.w("", "", e);
                            } catch (IllegalArgumentException e) {
                                MyLog.w("", "", e);
                            } catch (InvocationTargetException e) {
                                MyLog.w("", "", e);
                            }
                        }
                    } catch (JSONException e) {
                        MyLog.w("", "", e);
                    }
                    break;
                }
                case MSG_SET_RESULT_TO_JS:
                    MyLog.i(TAG, "MSG_SET_RESULT_TO_JS " + msg.getData().get(JsBridgeProtocol.EXTRA_KEY_RESULT));
                    WebView webView = (WebView) msg.obj;
                    Bundle data = msg.getData();
                    String result = data.getString(JsBridgeProtocol.EXTRA_KEY_RESULT);
                    InjectedWebViewClient.this.setResultToJs(webView, result);
                    break;
                default:
                    break;
            }
        }
    }
}
