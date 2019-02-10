package com.common.webview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import com.just.agentweb.AgentWeb;

public class AndroidCallBackInterface {
    private Handler deliver = new Handler(Looper.getMainLooper());
    private AgentWeb agent;
    private Context context;

    public AndroidCallBackInterface(AgentWeb agent, Context context) {
        this.agent = agent;
        this.context = context;
    }

    /**
     * 跳转的所有逻辑都走这个
     *
     * @param schema
     */
    @JavascriptInterface
    public void jumpCall(final String schema, boolean needFinish) {

    }

    /**
     * 提示
     *
     * @param msg
     */
    @JavascriptInterface
    public void toast(final String msg) {

    }
}
