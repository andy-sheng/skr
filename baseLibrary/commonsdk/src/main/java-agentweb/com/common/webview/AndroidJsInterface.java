package com.common.webview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.just.agentweb.AgentWeb;

public class AndroidJsInterface {
    private Handler deliver = new Handler(Looper.getMainLooper());
    private AgentWeb mAgentWeb;
    private Context context;

    public AndroidJsInterface(AgentWeb agent, Context context) {
        this.mAgentWeb = agent;
        this.context = context;
    }

    public void refresh(){
        mAgentWeb.getJsAccessEntrace().quickCallJs("refresh");
    }
}
